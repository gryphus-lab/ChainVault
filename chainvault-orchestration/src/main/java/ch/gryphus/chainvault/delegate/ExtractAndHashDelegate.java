/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Extract and hash delegate.
 */
@Slf4j
@Component("extractAndHash")
@RequiredArgsConstructor
public class ExtractAndHashDelegate implements JavaDelegate {

    private final MigrationService migrationService;
    private final AuditEventService auditEventService;

    @Override
    public void execute(DelegateExecution execution) {
        Span span = Span.current();
        String docId = (String) execution.getVariable("docId");
        span.setAttribute("document.id", docId);
        log.info("ExtractAndHashDelegate started for docId: {}", docId);

        String piKey = execution.getProcessInstanceId();
        String eventTaskType = "extract-hash";
        String errorCode = "EXTRACTION_FAILED";

        auditEventService.updateAuditEventStart(piKey, docId, eventTaskType);

        Map<String, Object> map;
        try {
            map = migrationService.extractAndHash(docId);

            // Record success event
            span.addEvent(
                    "extraction.success",
                    Attributes.of(AttributeKey.stringKey("document.id"), docId));

            execution.setTransientVariable("ctx", map.get("ctx"));
            execution.setTransientVariable("meta", map.get("meta"));
            execution.setTransientVariable("payload", map.get("payload"));

            auditEventService.updateAuditEventEnd(
                    piKey,
                    MigrationAudit.MigrationStatus.RUNNING,
                    null,
                    null,
                    eventTaskType,
                    "Extraction completed successfully");

        } catch (Exception e) {
            // Record failure event + exception
            span.addEvent(
                    "extraction.failed",
                    Attributes.of(
                            AttributeKey.stringKey("error.message"), e.getMessage(),
                            AttributeKey.stringKey("error.type"), e.getClass().getSimpleName()));

            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());

            // Update audit
            auditEventService.updateAuditEventEnd(
                    piKey,
                    MigrationAudit.MigrationStatus.FAILED,
                    errorCode,
                    e.getMessage(),
                    eventTaskType,
                    e.getMessage());

            // Throw BPMN error to trigger boundary event
            throw new BpmnError(errorCode, e.getMessage());
        }

        log.info("ExtractAndHashDelegate completed for docId: {}", docId);
    }
}
