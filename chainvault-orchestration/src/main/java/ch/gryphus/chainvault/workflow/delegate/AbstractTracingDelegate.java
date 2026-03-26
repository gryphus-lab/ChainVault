/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.model.dto.MigrationEventDto;
import ch.gryphus.chainvault.model.entity.MigrationAudit;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import ch.gryphus.chainvault.workflow.service.SseEmitterService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    private final SseEmitterService sseEmitterService;
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
            SseEmitterService sseEmitterService,
            String taskType,
            String errorCode) {
        this.openTelemetry = openTelemetry;
        this.auditService = auditService;
        this.sseEmitterService = sseEmitterService;
        this.taskType = taskType;
        this.errorCode = errorCode;
    }

    @Override
    public void execute(DelegateExecution execution) {
        // Use the utility to get the parent context
        String traceParent = getVariableSafely(execution, "traceParent", String.class);
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
            String docId =
                    getVariableSafely(execution, Constants.BPMN_PROC_VAR_DOC_ID, String.class);
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

            sendSseEvent(docId, span);

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

    private void sendSseEvent(String docId, Span span) {
        MigrationEventDto event = new MigrationEventDto();
        event.setId(UUID.randomUUID().toString());
        event.setMigrationId(docId);
        event.setEventType(taskType);
        event.setStepName(taskType);
        event.setMessage("%s completed successfully".formatted(taskType));
        event.setStatus("SUCCESS");
        event.setTimestamp(Instant.now());
        event.setTraceId(span.getSpanContext().getTraceId());
        sseEmitterService.sendEvent(event);
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

    /**
     * Gets transient variable safely.
     *
     * @param <T>             the type parameter
     * @param execution       the execution
     * @param variableName    the variable name
     * @param expectedRawType the expected raw type
     * @return the transient variable safely
     */
    @SuppressWarnings("unchecked")
    static <T> @Nullable T getTransientVariableSafely(
            DelegateExecution execution, String variableName, Class<T> expectedRawType) {
        Object value = execution.getTransientVariable(variableName);

        if (value == null) {
            return null;
        }

        // Verify the raw class
        if (!expectedRawType.isInstance(value)) {
            throw new IllegalArgumentException(
                    ("Variable '%s' = '%s' is of type %s, expected %s")
                            .formatted(
                                    variableName,
                                    value,
                                    value.getClass().getName(),
                                    expectedRawType.getName()));
        }

        // The warning is suppressed here, centralizing the risk
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T getVariableSafely(
            DelegateExecution execution, String variableName, Class<T> expectedRawType) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            return null;
        }

        // Verify the raw class
        if (!expectedRawType.isInstance(value)) {
            throw new IllegalArgumentException(
                    ("Variable '%s' = '%s' is of type %s, expected %s")
                            .formatted(
                                    variableName,
                                    value,
                                    value.getClass().getName(),
                                    expectedRawType.getName()));
        }

        // The warning is suppressed here, centralizing the risk
        return (T) value;
    }
}
