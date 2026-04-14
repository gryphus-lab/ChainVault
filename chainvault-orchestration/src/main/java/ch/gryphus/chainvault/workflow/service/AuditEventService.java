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
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
     * @param piKey    the pi key
     * @param docId    the doc id
     * @param taskType the task type
     * @param span     the span
     */
    public void updateAuditEventStart(String piKey, String docId, String taskType, Span span) {
        var audit = findAudit(piKey);
        String traceId = span.getSpanContext().getTraceId();

        audit.setDocumentId(docId);
        audit.setAttemptCount(audit.getAttemptCount() + 1);
        audit.setStatus(MigrationAudit.MigrationStatus.RUNNING);
        audit.setStartedAt(Instant.now());
        audit.setTraceId(traceId);
        auditRepo.save(audit);

        var event = new MigrationEvent();
        event.setMigrationAuditId(audit.getId());
        event.setEventType(MigrationEvent.MigrationEventType.TASK_STARTED);
        event.setTaskType(taskType);
        event.setTraceId(traceId);

        saveMigrationEvent(event);
    }

    /**
     * Update audit event end.
     *
     * @param piKey    the pi key
     * @param status   the status
     * @param code     the code
     * @param error    the error
     * @param taskType the task type
     * @param msg      the msg
     * @param varMap   the var map
     * @param span     the span
     */
    public void updateAuditEventEnd(
            String piKey,
            MigrationAudit.MigrationStatus status,
            String code,
            String error,
            String taskType,
            String msg,
            Map<String, Object> varMap,
            Span span) {
        var audit = findAudit(piKey);
        String traceId = span.getSpanContext().getTraceId();

        updateAuditDetails(audit, status, code, error, taskType, varMap);
        audit.setCompletedAt(Instant.now());
        audit.setTraceId(traceId);
        auditRepo.save(audit);

        var event = new MigrationEvent();
        event.setMigrationAuditId(audit.getId());
        event.setEventType(
                status == MigrationAudit.MigrationStatus.FAILED
                        ? MigrationEvent.MigrationEventType.TASK_FAILED
                        : MigrationEvent.MigrationEventType.TASK_COMPLETED);
        event.setTaskType(taskType);
        event.setMessage(msg);
        event.setErrorCode(code);
        event.setErrorMessage(error);
        event.setEventData(varMap);
        event.setTraceId(traceId);

        saveMigrationEvent(event);
    }

    /**
     * Handle exception.
     *
     * @param ex        the ex
     * @param span      the span
     * @param piKey     the pi key
     * @param errorCode the error code
     * @param taskType  the task type
     */
    public void handleException(
            Exception ex, Span span, String piKey, String errorCode, String taskType) {
        String message = ExceptionUtils.getMessage(ex);

        span.setStatus(StatusCode.ERROR, message);
        span.recordException(ex);
        span.addEvent(
                taskType + ".failed",
                Attributes.of(
                        AttributeKey.stringKey("error.message"),
                        message,
                        AttributeKey.stringKey("error.type"),
                        ex.getClass().getSimpleName()));

        updateAuditEventEnd(
                piKey,
                MigrationAudit.MigrationStatus.FAILED,
                errorCode,
                ExceptionUtils.getStackTrace(ex),
                taskType,
                message,
                Collections.emptyMap(),
                span);

        throw new BpmnError(errorCode, message);
    }

    private MigrationAudit findAudit(String piKey) {
        return auditRepo
                .findByProcessInstanceKey(piKey)
                .orElseThrow(() -> new IllegalStateException("No audit found for: " + piKey));
    }

    private void saveMigrationEvent(MigrationEvent event) {
        event.setCreatedAt(Instant.now());
        eventRepo.save(event);
    }

    private void updateAuditDetails(
            MigrationAudit audit,
            MigrationAudit.MigrationStatus status,
            String errorCode,
            String errorMsg,
            String eventTaskType,
            Map<String, Object> varMap) {
        audit.setStatus(status);

        if (status == MigrationAudit.MigrationStatus.FAILED) {
            applyFailureDetails(audit, errorCode, errorMsg, eventTaskType);
        }

        applyContextHashes(audit, (MigrationContext) varMap.get("migrationContext"));
        applyOcrResults(audit, varMap);

        Optional.ofNullable(varMap.get("outputFileKey"))
                .ifPresent(k -> audit.setOutputFileKey((String) k));
        Optional.ofNullable(varMap.get("chainOfCustodyZip"))
                .ifPresent(z -> audit.setChainOfCustodyZip(String.valueOf(z)));
    }

    private void applyFailureDetails(
            MigrationAudit audit, String errorCode, String errorMsg, String eventTaskType) {
        audit.setFailureReason(errorMsg);
        audit.setErrorCode(errorCode);

        if ("perform-ocr".equals(eventTaskType)) {
            audit.setOcrAttempted(true);
            audit.setOcrSuccess(false);
            audit.setOcrErrorCode("OCR_TESSERACT_ERROR");
            audit.setOcrErrorMessage(errorMsg);
        }
    }

    private void applyContextHashes(MigrationAudit audit, MigrationContext context) {
        if (context == null) return;
        Optional.ofNullable(context.getPayloadHash()).ifPresent(audit::setInputPayloadHash);
        Optional.ofNullable(context.getPdfHash()).ifPresent(audit::setMergedPdfHash);
    }

    private void applyOcrResults(MigrationAudit audit, Map<String, Object> varMap) {
        if (varMap.get("ocrResults") == null) return;

        audit.setOcrAttempted(true);
        audit.setOcrSuccess(true);
        audit.setOcrCompletedAt(Instant.now());

        // Properly serialize the List<String> instead of calling toString()
        Object ocrResultsObj = varMap.get("ocrResults");
        String ocrResultsText;
        if (ocrResultsObj instanceof List<?> ocrResultsList) {
            // Join list entries with newline separator for stable preview
            ocrResultsText = String.join("\n", ocrResultsList.stream()
                    .map(String::valueOf)
                    .toList());
        } else {
            // Fallback for unexpected types
            ocrResultsText = String.valueOf(ocrResultsObj);
        }

        audit.setOcrResultReference(StringUtils.abbreviate(ocrResultsText, 512));

        if (varMap.get("ocrPageCount") instanceof Integer count) audit.setOcrPageCount(count);
        if (varMap.get("ocrTextLength") instanceof Long length) audit.setOcrTotalTextLength(length);
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
        detail.setId(String.valueOf(audit.getId()));
        detail.setStatus(audit.getStatus() != null ? audit.getStatus().name() : null);
        detail.setDocId(audit.getDocumentId());
        detail.setCreatedAt(audit.getCreatedAt());
        detail.setUpdatedAt(audit.getLastUpdatedAt());
        detail.setOcrPageCount(audit.getOcrPageCount());
        detail.setOcrAttempted(audit.getOcrAttempted());
        detail.setOcrSuccess(audit.getOcrSuccess());
        detail.setOcrTotalTextLength(audit.getOcrTotalTextLength());
        detail.setOcrTextPreview(audit.getOcrResultReference());
        detail.setTraceId(audit.getTraceId());
        detail.setEvents(eventRepo.getAllByMigrationAuditId((audit.getId())));
        detail.setChainZipUrl(audit.getChainOfCustodyZip());
        detail.setPdfUrl(audit.getOutputFileKey());
        return detail;
    }
}