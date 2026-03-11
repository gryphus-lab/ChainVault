/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.entity.MigrationEvent;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.repository.MigrationEventRepository;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
     * @param span              the span
     */
    public void updateAuditEventStart(
            String processInstanceId, String docId, String eventTaskType, Span span) {

        var audit =
                auditRepo
                        .findByProcessInstanceKey(processInstanceId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No audit for %s".formatted(processInstanceId)));

        String traceId = span.getSpanContext().getTraceId();

        audit.setProcessInstanceKey(processInstanceId);
        audit.setDocumentId(docId);
        audit.setAttemptCount(audit.getAttemptCount() + 1);
        audit.setStatus(MigrationAudit.MigrationStatus.RUNNING);
        audit.setStartedAt(Instant.now());
        audit.setTraceId(traceId);
        auditRepo.save(audit);

        var event = new MigrationEvent();
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
     * @param varMap            the var map
     */
    public void updateAuditEventEnd(
            String processInstanceId,
            MigrationAudit.MigrationStatus status,
            String errorCode,
            String errorMsg,
            String eventTaskType,
            String eventMsg,
            Map<String, Object> varMap) {
        var audit =
                auditRepo
                        .findByProcessInstanceKey(processInstanceId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No audit for %s".formatted(processInstanceId)));

        audit.setStatus(status);

        if (status == MigrationAudit.MigrationStatus.FAILED) {
            audit.setFailureReason(errorMsg);
            audit.setErrorCode(errorCode);
        }

        if (varMap.get("outputFileKey") != null) {
            audit.setOutputFileKey((String) varMap.get("outputFileKey"));
        }
        if (varMap.get("chainOfCustodyZip") != null) {
            audit.setChainOfCustodyZip(String.valueOf(varMap.get("chainOfCustodyZip")));
        }

        var ctx = (MigrationContext) varMap.get("ctx");
        if (ctx != null) {
            if (ctx.getPayloadHash() != null) {
                audit.setInputPayloadHash(ctx.getPayloadHash());
            }
            if (ctx.getPdfHash() != null) {
                audit.setMergedPdfHash(ctx.getPdfHash());
            }
        }

        String traceId = Span.current().getSpanContext().getTraceId();

        audit.setCompletedAt(Instant.now());
        audit.setTraceId(traceId);
        auditRepo.save(audit);

        var event = new MigrationEvent();
        event.setMigrationAuditId(audit.getId());

        event.setEventType(
                status == MigrationAudit.MigrationStatus.FAILED
                        ? MigrationEvent.MigrationEventType.TASK_FAILED
                        : MigrationEvent.MigrationEventType.TASK_COMPLETED);

        event.setTaskType(eventTaskType);

        if (errorMsg != null) {
            event.setErrorCode(errorCode);
            event.setErrorMessage(errorMsg);
        }

        event.setMessage(eventMsg);
        event.setTraceId(traceId);
        eventRepo.save(event);
    }

    /**
     * Handle exception.
     *
     * @param exception     the exception
     * @param span          the span
     * @param piKey         the pi key
     * @param errorCode     the error code
     * @param eventTaskType the event task type
     */
    public void handleException(
            Exception exception, Span span, String piKey, String errorCode, String eventTaskType) {
        // Record failure event + exception
        span.addEvent(
                "%s.failed".formatted(eventTaskType),
                Attributes.of(
                        AttributeKey.stringKey("error.message"), exception.getMessage(),
                        AttributeKey.stringKey("error.type"),
                                exception.getClass().getSimpleName()));

        span.recordException(exception);
        span.setStatus(StatusCode.ERROR, exception.getMessage());

        // Update audit with error details
        updateAuditEventEnd(
                piKey,
                MigrationAudit.MigrationStatus.FAILED,
                errorCode,
                ExceptionUtils.getStackTrace(exception),
                eventTaskType,
                ExceptionUtils.getMessage(exception),
                Collections.emptyMap());

        // Throw BPMN error to trigger boundary event
        throw new BpmnError(errorCode, exception.getMessage());
    }
}
