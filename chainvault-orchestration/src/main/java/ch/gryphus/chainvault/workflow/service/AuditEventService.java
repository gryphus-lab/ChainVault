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
     * Mark the migration audit identified by piKey as started and record a TASK_STARTED event.
     *
     * Sets the audit's document id, increments its attempt count, sets status to RUNNING,
     * records the start time and the span's trace id, persists the audit, and creates a TASK_STARTED MigrationEvent.
     *
     * @param piKey    the process instance key used to locate the MigrationAudit
     * @param docId    the document id to associate with the audit
     * @param taskType the task type to record on the created MigrationEvent
     * @param span     the OpenTelemetry span from which the trace id is extracted
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
     * Finalize an audit record and record a corresponding migration event.
     *
     * Updates the MigrationAudit identified by the given process-instance key with the provided status,
     * completion metadata, and any details derived from varMap, then creates and persists a MigrationEvent
     * describing the task completion or failure.
     *
     * @param piKey    the process-instance key used to locate the MigrationAudit
     * @param status   the final migration status to set on the audit
     * @param code     an error code associated with the task (may be null for success)
     * @param error    a detailed error payload (typically the stack trace) or null
     * @param taskType the logical task type/name for the event (e.g., "perform-ocr")
     * @param msg      a short human-readable message describing the event outcome
     * @param varMap   contextual variables produced by the task (may be inspected to populate audit fields)
     * @param span     the OpenTelemetry span from which the trace id will be extracted
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
     * Record the provided exception on the supplied tracing span, mark the related migration audit as failed, and throw a BPMN error.
     *
     * @param ex        the exception that occurred
     * @param span      the OpenTelemetry span to update with error status, recorded exception, and a "<taskType>.failed" event
     * @param piKey     the process-instance key identifying the migration audit to update
     * @param errorCode the error code to store on the audit event and to use for the thrown BPMN error
     * @param taskType  the task identifier used for the span event name and audit event
     * @throws BpmnError thrown with {@code errorCode} and the exception message after the audit and span are updated
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

    /**
     * Retrieve the MigrationAudit associated with the given process instance key.
     *
     * @param piKey the process instance key used to look up the audit
     * @return the matching MigrationAudit
     * @throws IllegalStateException if no audit is found for the given key
     */
    private MigrationAudit findAudit(String piKey) {
        return auditRepo
                .findByProcessInstanceKey(piKey)
                .orElseThrow(() -> new IllegalStateException("No audit found for: " + piKey));
    }

    /**
     * Persist the given MigrationEvent after setting its creation timestamp to the current instant.
     *
     * @param event the MigrationEvent to stamp with a creation time and save
     */
    private void saveMigrationEvent(MigrationEvent event) {
        event.setCreatedAt(Instant.now());
        eventRepo.save(event);
    }

    /**
     * Updates a MigrationAudit with status and supplemental details derived from the provided context and variables.
     *
     * Sets the audit's status, applies failure metadata when status is FAILED, extracts and applies context hashes
     * from the "migrationContext" entry, applies OCR-derived results from the map, and optionally sets
     * outputFileKey and chainOfCustodyZip if present.
     *
     * @param audit the audit record to update
     * @param status the new migration status to set on the audit
     * @param errorCode error code to record when applying failure details (used only if status is FAILED)
     * @param errorMsg error message/stack trace to record when applying failure details (used only if status is FAILED)
     * @param eventTaskType the task type that produced the event (used for task-specific failure handling)
     * @param varMap a map of contextual variables; expected keys include:
     *               "migrationContext" (MigrationContext) for payload/pdf hashes,
     *               "ocrResults"/"ocrPageCount"/"ocrTextLength" for OCR result application,
     *               "outputFileKey" (String) and "chainOfCustodyZip" (any) for optional output references
     */
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
        } else {
            // Clear failure-specific fields for recovered migrations
            audit.setFailureReason(null);
            audit.setErrorCode(null);
        }

        applyContextHashes(audit, (MigrationContext) varMap.get("migrationContext"));
        applyOcrResults(audit, varMap);

        Optional.ofNullable(varMap.get("outputFileKey"))
                .ifPresent(k -> audit.setOutputFileKey((String) k));
        Optional.ofNullable(varMap.get("chainOfCustodyZip"))
                .ifPresent(z -> audit.setChainOfCustodyZip(String.valueOf(z)));
    }

    /**
     * Record failure information on the provided MigrationAudit and apply OCR-specific failure details
     * if the failing task type is "perform-ocr".
     *
     * @param audit the audit record to update
     * @param errorCode the error code to store on the audit
     * @param errorMsg the failure message or stack trace to store as the failure reason
     * @param eventTaskType the task type that failed; if equal to "perform-ocr", OCR failure fields are set
     */
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

    /**
     * Copies payload and PDF hash values from the migration context into the audit when present.
     *
     * If `context` is null nothing is changed; otherwise `context.getPayloadHash()` is set to
     * `audit.inputPayloadHash` and `context.getPdfHash()` is set to `audit.mergedPdfHash` when those values are non-null.
     *
     * @param audit   the audit record to update
     * @param context the migration context that may contain `payloadHash` and `pdfHash`; may be null
     */
    private void applyContextHashes(MigrationAudit audit, MigrationContext context) {
        if (context == null) return;
        Optional.ofNullable(context.getPayloadHash()).ifPresent(audit::setInputPayloadHash);
        Optional.ofNullable(context.getPdfHash()).ifPresent(audit::setMergedPdfHash);
    }

    /**
     * Applies OCR-derived results from the provided variables map to the given audit, recording attempt and success flags, completion time, a truncated OCR text preview, and optional page/count metrics.
     *
     * @param audit the MigrationAudit to update
     * @param varMap a map that may contain:
     *               <ul>
     *                 <li><code>"ocrResults"</code>: a List or other object used to generate the OCR preview (list entries are joined with '\n');</li>
     *                 <li><code>"ocrPageCount"</code>: an Integer to set the page count;</li>
     *                 <li><code>"ocrTextLength"</code>: a Long to set the total OCR text length.</li>
     *               </ul>
     */
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
            ocrResultsText =
                    String.join("\n", ocrResultsList.stream().map(String::valueOf).toList());
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
     * Retrieve detailed migration information for the audit with the given id.
     *
     * @param id the audit id as a decimal string
     * @return a MigrationDetail populated with audit metadata (id, status, document id, created/updated timestamps), OCR fields (page count, attempted, success, total text length, text preview), trace id, associated events, chain-of-custody zip URL, and output PDF URL
     * @throws EntityNotFoundException if no audit exists for the provided id
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
