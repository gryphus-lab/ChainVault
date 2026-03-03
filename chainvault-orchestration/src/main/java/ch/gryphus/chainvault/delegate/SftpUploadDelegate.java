/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AuditEventService auditEventService;

    @Override
    public void execute(DelegateExecution execution) {
        Span span = Span.current();
        String docId = (String) execution.getVariable("docId");
        span.setAttribute("document.id", docId);
        log.info("SftpUploadDelegate started for docId: {}", docId);

        String piKey = execution.getProcessInstanceId();
        String eventTaskType = "upload-sftp";
        String errorCode = "UPLOAD_FAILED";

        auditEventService.updateAuditEventStart(piKey, docId, eventTaskType);

        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");
        String xml = (String) execution.getTransientVariable("xml");
        Path zipPath = (Path) execution.getTransientVariable("zipPath");
        Path pdfPath = (Path) execution.getTransientVariable("pdfPath");

        try {
            migrationService.uploadToSftp(ctx, docId, xml, zipPath, pdfPath);

            // Record success event
            span.addEvent(
                    "%s.success".formatted(eventTaskType),
                    Attributes.of(AttributeKey.stringKey("document.id"), docId));

            // Update audit
            auditEventService.updateAuditEventEnd(
                    piKey,
                    MigrationAudit.MigrationStatus.SUCCESS,
                    null,
                    null,
                    eventTaskType,
                    "Sftp upload completed successfully");
        } catch (Exception e) {
            auditEventService.handleException(e, span, piKey, errorCode, eventTaskType);
        }

        log.info("SftpUploadDelegate completed for docId: {}", docId);
    }
}
