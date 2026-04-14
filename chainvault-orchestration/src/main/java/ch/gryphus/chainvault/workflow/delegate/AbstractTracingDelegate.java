/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.model.dto.MigrationEventDto;
import ch.gryphus.chainvault.model.entity.MigrationAudit;
import ch.gryphus.chainvault.service.SseEmitterService;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
    private final SseEmitterService sseEmitterService;
    private final String taskType;
    private final String errorCode;

    /**
     * Instantiates a new Abstract tracing delegate.
     *
     * @param openTelemetry     the open telemetry
     * @param auditService      the audit service
     * @param sseEmitterService the sse emitter service
     * @param taskType          the task type
     * @param errorCode         the error code
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

    /**
     * Executes the delegate within an OpenTelemetry child span: starts an audit event, invokes
     * subclass task logic, collects filtered transient outputs, emits an SSE event with the task
     * status, and finalizes the audit event. Exceptions are recorded on the span and routed to the
     * audit service; the span is always ended.
     *
     * @param execution the BPMN delegate execution providing process and transient variables
     */
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
        String processInstanceId = execution.getProcessInstanceId();

        try (var _ = span.makeCurrent()) {
            log.info("{} started", taskType);
            String docId =
                    getVariableSafely(execution, Constants.BPMN_PROC_VAR_DOC_ID, String.class);
            auditService.updateAuditEventStart(processInstanceId, docId, taskType, span);

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

            var status =
                    Objects.equals(taskType, "handle-error")
                            ? MigrationAudit.MigrationStatus.FAILED
                            : MigrationAudit.MigrationStatus.SUCCESS;

            sendSseEvent(processInstanceId, span, status);

            auditService.updateAuditEventEnd(
                    processInstanceId,
                    status,
                    null,
                    null,
                    taskType,
                    status == MigrationAudit.MigrationStatus.FAILED ? "Failure" : "Success",
                    outputMap,
                    span);

            log.info("{} finished", taskType);
        } catch (Exception e) {
            log.error("{} encountered an exception", taskType, e);
            span.recordException(e);

            sendSseEvent(processInstanceId, span, MigrationAudit.MigrationStatus.FAILED);
            auditService.handleException(e, span, processInstanceId, errorCode, taskType);
        } finally {
            span.end();
        }
    }

    private void sendSseEvent(
            String piKey, @NonNull Span span, MigrationAudit.MigrationStatus status) {
        log.info("{} sending SSE event", taskType);
        String message =
                status == MigrationAudit.MigrationStatus.FAILED
                        ? "%s failed".formatted(taskType)
                        : "%s completed successfully".formatted(taskType);
        MigrationEventDto event = new MigrationEventDto();
        event.setId(UUID.randomUUID().toString());
        event.setMigrationId(piKey);
        event.setEventType(taskType);
        event.setStepName(taskType);
        event.setMessage(message);
        event.setStatus(String.valueOf(status));
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