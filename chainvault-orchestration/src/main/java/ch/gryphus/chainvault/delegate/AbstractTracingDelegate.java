/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * The type Abstract tracing delegate.
 */
public abstract class AbstractTracingDelegate implements JavaDelegate {

    // Inject these once in the constructor of your child classes
    private final AuditEventService auditService;
    private final String taskType;
    private final String errorCode;

    /**
     * Instantiates a new Abstract tracing delegate.
     *
     * @param auditService the audit service
     * @param taskType     the task type
     * @param errorCode    the error code
     */
    protected AbstractTracingDelegate(
            AuditEventService auditService, String taskType, String errorCode) {
        this.auditService = auditService;
        this.taskType = taskType;
        this.errorCode = errorCode;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String traceParent = (String) execution.getVariable("traceParent");
        Context parentContext = OTelUtils.extractContextFromTraceParent(traceParent);

        Span span =
                GlobalOpenTelemetry.getTracer("chainvault-tracer")
                        .spanBuilder(taskType)
                        .setParent(parentContext)
                        .startSpan();

        try (Scope scope = span.makeCurrent()) {
            String docId = (String) execution.getVariable(Constants.BPMN_PROC_VAR_DOC_ID);
            auditService.updateAuditEventStart(
                    execution.getProcessInstanceId(), docId, taskType, span);

            doExecute(execution, span, docId); // Delegate logic

            span.addEvent(taskType + ".success");
            auditService.updateAuditEventEnd(
                    execution.getProcessInstanceId(),
                    MigrationAudit.MigrationStatus.SUCCESS,
                    null,
                    null,
                    taskType,
                    "Success",
                    execution.getTransientVariables());
        } catch (Exception e) {
            span.recordException(e);
            auditService.handleException(
                    e, span, execution.getProcessInstanceId(), errorCode, taskType);
        } finally {
            span.end();
        }
    }

    /**
     * Do execute.
     *
     * @param execution the execution
     * @param span      the span
     * @param docId     the doc id
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws IOException              the io exception
     */
    protected abstract void doExecute(DelegateExecution execution, Span span, String docId)
            throws NoSuchAlgorithmException, IOException;
}
