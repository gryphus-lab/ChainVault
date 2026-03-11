/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The type Abstract tracing delegate.
 */
public abstract class AbstractTracingDelegate implements JavaDelegate {

    @Autowired private OpenTelemetry openTelemetry;

    private final AuditEventService auditService;
    private final String taskType;
    private final String errorCode;

    protected AbstractTracingDelegate(
            AuditEventService auditService, String taskType, String errorCode) {
        this.auditService = auditService;
        this.taskType = taskType;
        this.errorCode = errorCode;
    }

    @Override
    public void execute(DelegateExecution execution) {
        // Use the utility to get the parent context
        String traceParent = (String) execution.getVariable("traceParent");
        Context parentContext = OTelUtils.extractContext(openTelemetry, traceParent);

        // Start Child Span
        Span span =
                openTelemetry
                        .getTracer("chainvault")
                        .spanBuilder(taskType)
                        .setParent(parentContext)
                        .startSpan();

        try (Scope scope = span.makeCurrent()) {
            String docId = (String) execution.getVariable(Constants.BPMN_PROC_VAR_DOC_ID);
            auditService.updateAuditEventStart(
                    execution.getProcessInstanceId(), docId, taskType, span);

            doExecute(execution, span, docId);

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

    protected abstract void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException;
}
