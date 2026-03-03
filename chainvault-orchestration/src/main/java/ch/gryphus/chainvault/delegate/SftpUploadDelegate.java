/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import java.nio.file.Path;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Sftp upload delegate.
 */
@Slf4j
@Component("uploadSftp")
@RequiredArgsConstructor
public class SftpUploadDelegate implements JavaDelegate {
    private final MigrationService migrationService;
    private final MigrationAuditRepository auditRepo;

    @Override
    public void execute(DelegateExecution execution) {
        Span span = Span.current();
        String docId = (String) execution.getVariable("docId");

        String piKey = execution.getProcessInstanceId();

        log.info("SftpUploadDelegate started for docId: {}", docId);

        // Add attributes to the current span
        span.setAttribute("document.id", docId);

        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");
        String xml = (String) execution.getTransientVariable("xml");
        Path zipPath = (Path) execution.getTransientVariable("zipPath");
        Path pdfPath = (Path) execution.getTransientVariable("pdfPath");

        try {
            migrationService.uploadToSftp(ctx, docId, xml, zipPath, pdfPath);

            // Update audit
            updateAudit(piKey, MigrationAudit.MigrationStatus.SUCCESS, null);
        } catch (Exception e) {
            // Record failure event + exception
            span.addEvent(
                    "sftpUpload.failed",
                    Attributes.of(
                            AttributeKey.stringKey("error.message"), e.getMessage(),
                            AttributeKey.stringKey("error.type"), e.getClass().getSimpleName()));

            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());

            // Update audit
            updateAudit(piKey, MigrationAudit.MigrationStatus.FAILED, e.getMessage());

            // Throw BPMN error to trigger boundary event
            throw new BpmnError("UPLOAD_FAILED", e.getMessage());
        }

        log.info("SftpUploadDelegate completed for docId: {}", docId);
    }

    private void updateAudit(String piKey, MigrationAudit.MigrationStatus status, String errorMsg) {

        MigrationAudit audit =
                auditRepo
                        .findByProcessInstanceKey(piKey)
                        .orElseThrow(() -> new IllegalStateException("No audit for " + piKey));

        audit.setStatus(status);
        if (status == MigrationAudit.MigrationStatus.FAILED) {
            audit.setFailureReason(errorMsg);
            audit.setErrorCode("EXTRACTION_FAILED");
        }
        audit.setCompletedAt(Instant.now());
        audit.setTraceId(Span.current().getSpanContext().getTraceId());

        auditRepo.save(audit);
    }
}
