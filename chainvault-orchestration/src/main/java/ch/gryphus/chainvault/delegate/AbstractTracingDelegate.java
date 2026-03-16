/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * The type Abstract tracing delegate.
 */
@Slf4j
public abstract class AbstractTracingDelegate implements JavaDelegate {

    private final OpenTelemetry openTelemetry;
    private final AuditEventService auditService;
    private final String taskType;
    private final String errorCode;

    /**
     * Instantiates a new Abstract tracing delegate.
     *
     * @param openTelemetry the open telemetry
     * @param auditService  the audit service
     * @param taskType      the task type
     * @param errorCode     the error code
     */
    protected AbstractTracingDelegate(
            OpenTelemetry openTelemetry,
            AuditEventService auditService,
            String taskType,
            String errorCode) {
        this.openTelemetry = openTelemetry;
        this.auditService = auditService;
        this.taskType = taskType;
        this.errorCode = errorCode;
    }

    @Override
    public void execute(@NonNull DelegateExecution execution) {
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

        try (var _ = span.makeCurrent()) {
            log.info("{} started", taskType);
            String docId = (String) execution.getVariable(Constants.BPMN_PROC_VAR_DOC_ID);
            auditService.updateAuditEventStart(
                    execution.getProcessInstanceId(), docId, taskType, span);

            doExecute(execution, span, docId);

            // filter out byte array payloads and TiffPages instances
            Map<String, Object> outputMap = new HashMap<>();
            execution
                    .getTransientVariables()
                    .forEach(
                            (key, value) -> {
                                if (!(value instanceof byte[]) && !"pages".equals(key)) {
                                    outputMap.put(key, value);
                                }
                            });
            auditService.updateAuditEventEnd(
                    execution.getProcessInstanceId(),
                    MigrationAudit.MigrationStatus.SUCCESS,
                    null,
                    null,
                    taskType,
                    "Success",
                    outputMap);
            log.info("{} finished", taskType);
        } catch (Exception e) {
            log.error("{} encountered an exception", taskType, e);
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
     * @throws IOException              the io exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws TesseractException       the tesseract exception
     */
    protected abstract void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException, TesseractException;
}
