/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Sign document delegate.
 */
@Slf4j
@Component("signDocument")
@RequiredArgsConstructor
public class SignDocumentDelegate implements JavaDelegate {

    private final MigrationService migrationService;
    private final MigrationAuditRepository auditRepo;

    @Override
    public void execute(DelegateExecution execution) {
        Span span = Span.current();
        String docId = (String) execution.getVariable("docId");

        log.info("SignDocumentDelegate started for docId: {}", docId);
        String piKey = execution.getProcessInstanceId();

        // Add attributes to the current span
        span.setAttribute("document.id", docId);

        byte[] payload = (byte[]) execution.getTransientVariable("payload");
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

        List<TiffPage> pages;
        try {
            pages = migrationService.signTiffPages(payload, ctx);

            // Record success event
            span.addEvent(
                    "signTiffs.success",
                    Attributes.of(AttributeKey.stringKey("document.id"), docId));

            execution.setTransientVariable("pages", pages);

            // Update audit
            updateAudit(piKey, MigrationAudit.MigrationStatus.RUNNING, null);
        } catch (Exception e) {
            // Record failure event + exception
            span.addEvent(
                    "signTiffs.failed",
                    Attributes.of(
                            AttributeKey.stringKey("error.message"), e.getMessage(),
                            AttributeKey.stringKey("error.type"), e.getClass().getSimpleName()));

            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());

            // Update audit
            updateAudit(piKey, MigrationAudit.MigrationStatus.FAILED, e.getMessage());

            // Throw BPMN error to trigger boundary event
            throw new BpmnError("SIGN_FAILED", e.getMessage());
        }

        log.info("SignDocumentDelegate completed for docId: {}", docId);
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
