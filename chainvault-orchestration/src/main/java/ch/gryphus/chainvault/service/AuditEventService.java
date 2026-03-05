/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.entity.MigrationEvent;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.repository.MigrationEventRepository;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.BpmnError;
import org.springframework.stereotype.Service;

/**
 * The type Audit event service.
 */
@Service
@RequiredArgsConstructor
public class AuditEventService {

    private final MigrationAuditRepository auditRepo;
    private final MigrationEventRepository eventRepo;

    /**
     * Update audit event start.
     *
     * @param processInstanceId the process instance id
     * @param docId             the doc id
     * @param eventTaskType     the event task type
     * @param span
     */
    public void updateAuditEventStart(
            String processInstanceId, String docId, String eventTaskType, Span span) {

        MigrationAudit audit =
                auditRepo
                        .findByProcessInstanceKey(processInstanceId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No audit for " + processInstanceId));

        String traceId = span.getSpanContext().getTraceId();

        audit.setProcessInstanceKey(processInstanceId);
        audit.setDocumentId(docId);
        audit.setAttemptCount(audit.getAttemptCount() + 1);
        audit.setStatus(MigrationAudit.MigrationStatus.RUNNING);
        audit.setStartedAt(Instant.now());
        audit.setTraceId(traceId);
        auditRepo.save(audit);

        MigrationEvent event = new MigrationEvent();
        event.setMigrationAuditId(audit.getId());
        event.setEventType(MigrationEvent.MigrationEventType.TASK_STARTED);
        event.setTaskType(eventTaskType);
        event.setCreatedAt(Instant.now());
        event.setTraceId(traceId);
        eventRepo.save(event);
    }

    /**
     * Update audit event end.
     *
     * @param processInstanceId the process instance id
     * @param status            the status
     * @param errorCode         the error code
     * @param errorMsg          the error msg
     * @param eventTaskType     the event task type
     * @param eventMsg          the event msg
     * @param span
     */
    public void updateAuditEventEnd(
            String processInstanceId,
            MigrationAudit.MigrationStatus status,
            String errorCode,
            String errorMsg,
            String eventTaskType,
            String eventMsg, Span span) {
        MigrationAudit audit =
                auditRepo
                        .findByProcessInstanceKey(processInstanceId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No audit for " + processInstanceId));

        audit.setStatus(status);

        if (status == MigrationAudit.MigrationStatus.FAILED) {
            audit.setFailureReason(errorMsg);
            audit.setErrorCode(errorCode);
        }
        String traceId = Span.current().getSpanContext().getTraceId();

        audit.setCompletedAt(Instant.now());
        audit.setTraceId(traceId);
        auditRepo.save(audit);

        MigrationEvent event = new MigrationEvent();
        event.setMigrationAuditId(audit.getId());

        event.setEventType(
                status == MigrationAudit.MigrationStatus.FAILED
                        ? MigrationEvent.MigrationEventType.TASK_FAILED
                        : MigrationEvent.MigrationEventType.TASK_COMPLETED);

        event.setTaskType(eventTaskType);
        event.setMessage(eventMsg);
        event.setTraceId(traceId);
        eventRepo.save(event);
    }

    /**
     * Handle exception.
     *
     * @param e the e
     * @param span the span
     * @param piKey the pi key
     * @param errorCode the error code
     * @param eventTaskType the event task type
     */
    public void handleException(
            Exception e, Span span, String piKey, String errorCode, String eventTaskType) {
        // Record failure event + exception
        span.addEvent(
                "%s.failed".formatted(eventTaskType),
                Attributes.of(
                        AttributeKey.stringKey("error.message"), e.getMessage(),
                        AttributeKey.stringKey("error.type"), e.getClass().getSimpleName()));

        span.recordException(e);
        span.setStatus(StatusCode.ERROR, e.getMessage());

        // Update audit
        updateAuditEventEnd(
                piKey,
                MigrationAudit.MigrationStatus.FAILED,
                errorCode,
                e.getMessage(),
                eventTaskType,
                e.getMessage(), span);

        // Throw BPMN error to trigger boundary event
        throw new BpmnError(errorCode, e.getMessage());
    }
}
