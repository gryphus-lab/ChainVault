/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.service;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.model.dto.Migration;
import ch.gryphus.chainvault.model.dto.MigrationDetail;
import ch.gryphus.chainvault.model.dto.MigrationStats;
import ch.gryphus.chainvault.model.entity.MigrationAudit;
import ch.gryphus.chainvault.model.entity.MigrationEvent;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.repository.MigrationEventRepository;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.flowable.engine.delegate.BpmnError;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Audit event service.
 */
@Service
@RequiredArgsConstructor
@Transactional(noRollbackForClassName = {"org.flowable.engine.delegate.BpmnError"})
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
            // add error details to audit
            audit.setFailureReason(errorMsg);
            audit.setErrorCode(errorCode);

            // add OCR related error details
            if (Objects.equals(eventTaskType, "perform-ocr")) {
                audit.setOcrAttempted(true);
                audit.setOcrSuccess(false);
                audit.setOcrErrorCode("OCR_TESSERACT_ERROR");
                audit.setOcrErrorMessage(errorMsg);
            }
        }

        var migrationContext = (MigrationContext) varMap.get("migrationContext");
        if (migrationContext != null) {
            if (migrationContext.getPayloadHash() != null) {
                audit.setInputPayloadHash(migrationContext.getPayloadHash());
            }
            if (migrationContext.getPdfHash() != null) {
                audit.setMergedPdfHash(migrationContext.getPdfHash());
            }
        }

        // Update OCR related audit table fields
        if (varMap.get("ocrResults") != null) {
            audit.setOcrAttempted(true);
            audit.setOcrCompletedAt(Instant.now());
            audit.setOcrPageCount((Integer) varMap.get("ocrPageCount"));
            audit.setOcrSuccess(true);
            int ocrTextLength = (int) varMap.get("ocrTextLength");
            audit.setOcrTotalTextLength((long) ocrTextLength);
        }

        if (varMap.get("outputFileKey") != null) {
            audit.setOutputFileKey((String) varMap.get("outputFileKey"));
        }
        if (varMap.get("chainOfCustodyZip") != null) {
            audit.setChainOfCustodyZip(String.valueOf(varMap.get("chainOfCustodyZip")));
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

        event.setEventData(varMap);
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
                ExceptionUtils.getStackTrace(exception), // store stack trace
                eventTaskType,
                ExceptionUtils.getMessage(exception), // store error message
                Collections.emptyMap());

        // Throw BPMN error to trigger boundary event
        throw new BpmnError(errorCode, exception.getMessage());
    }

    /**
     * Gets migrations.
     *
     * @param limit the limit
     * @return the migrations
     */
    public List<Migration> getMigrations(int limit) {
        List<MigrationAudit> auditRecords = auditRepo.getAllByCompletedAtIsNotNull(Limit.of(limit));
        List<Migration> migrations = new ArrayList<>();
        auditRecords.forEach(
                audit -> {
                    Migration m = new Migration();
                    m.setId(String.valueOf(audit.getId()));
                    m.setProcessInstanceKey(audit.getProcessInstanceKey());
                    m.setDocId(audit.getDocumentId());
                    m.setStatus(String.valueOf(audit.getStatus()));
                    m.setCreatedAt(audit.getCreatedAt());
                    m.setUpdatedAt(audit.getLastUpdatedAt());
                    m.setTraceId(audit.getTraceId());
                    m.setOcrPageCount(audit.getOcrPageCount());
                    m.setOcrAttempted(audit.getOcrAttempted());
                    m.setOcrSuccess(audit.getOcrSuccess());
                    m.setOcrTotalTextLength(audit.getOcrTotalTextLength());
                    migrations.add(m);
                });
        return migrations;
    }

    /**
     * Gets stats.
     *
     * @return the stats
     */
    public MigrationStats getStats() {
        MigrationStats stats = new MigrationStats();
        stats.setTotal(auditRepo.count());
        stats.setSuccess(auditRepo.countAllByStatus(MigrationAudit.MigrationStatus.SUCCESS));
        stats.setFailed(auditRepo.countAllByStatus(MigrationAudit.MigrationStatus.FAILED));
        stats.setPending(auditRepo.countAllByStatus(MigrationAudit.MigrationStatus.PENDING));
        stats.setRunning(auditRepo.countAllByStatus(MigrationAudit.MigrationStatus.RUNNING));
        return stats;
    }

    /**
     * Gets detail.
     *
     * @param id the id
     * @return the detail
     */
    public MigrationDetail getDetail(String id) {
        MigrationDetail detail = new MigrationDetail();
        MigrationAudit audit =
                auditRepo
                        .findById(Long.valueOf(id))
                        .orElseThrow(
                                () -> new EntityNotFoundException("Migration not found: " + id));
        detail.setDocId(audit.getDocumentId());
        detail.setCreatedAt(audit.getCreatedAt());
        detail.setOcrPageCount(audit.getOcrPageCount());
        detail.setOcrAttempted(audit.getOcrAttempted());
        detail.setOcrSuccess(audit.getOcrSuccess());
        detail.setOcrTotalTextLength(audit.getOcrTotalTextLength());
        detail.setTraceId(audit.getTraceId());
        detail.setEvents(eventRepo.getAllByMigrationAuditId((audit.getId())));
        detail.setChainZipUrl(audit.getChainOfCustodyZip());
        detail.setPdfUrl(audit.getOutputFileKey());
        return detail;
    }
}
