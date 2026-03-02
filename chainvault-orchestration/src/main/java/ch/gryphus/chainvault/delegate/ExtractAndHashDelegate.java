/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.entity.MigrationEvent;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.repository.MigrationEventRepository;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.trace.Span;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Extract and hash delegate.
 */
@Slf4j
@Component("extractAndHash")
public class ExtractAndHashDelegate implements JavaDelegate {

    private final MigrationService migrationService;
    private final MigrationEventRepository migrationEventRepository;
    private final MigrationAuditRepository auditRepository;

    /**
     * Instantiates a new Extract and hash delegate.
     *
     * @param migrationService the migration service
     */
    public ExtractAndHashDelegate(
            MigrationService migrationService,
            MigrationEventRepository migrationEventRepository,
            MigrationAuditRepository auditRepository) {
        this.migrationService = migrationService;
        this.migrationEventRepository = migrationEventRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String docId = (String) execution.getVariable("docId");

        log.info("ExtractAndHashDelegate started for docId: {}", docId);

        Map<String, Object> map;
        try {
            map = migrationService.extractAndHash(docId);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("hash algorithm not available", e);
        }

        execution.setTransientVariable("ctx", map.get("ctx"));
        execution.setTransientVariable("meta", map.get("meta"));
        execution.setTransientVariable("payload", map.get("payload"));
        Long piKey = Long.valueOf(execution.getProcessInstanceId());
        MigrationAudit audit =
                auditRepository
                        .findByProcessInstanceKey(piKey)
                        .orElseThrow(() -> new IllegalStateException("No audit for " + piKey));
        audit.setStatus(MigrationAudit.MigrationStatus.RUNNING);
        audit.setAttemptCount(audit.getAttemptCount() + 1);
        audit.setStartedAt(Instant.now());
        auditRepository.save(audit);

        MigrationContext ctx = (MigrationContext) map.get("ctx");
        MigrationEvent event =
                MigrationEvent.builder()
                        .migrationAuditId(audit.getId())
                        .eventType(MigrationEvent.MigrationEventType.TASK_COMPLETED)
                        .taskType("extract-hash")
                        .message("Successfully extracted and hashed TIFF pages")
                        .createdAt(Instant.now())
                        .eventData(
                                Map.of(
                                        "pageCount", ctx.getPageHashes().size(),
                                        "payloadHash", ctx.getPayloadHash()))
                        .traceId(Span.current().getSpanContext().getTraceId())
                        .build();

        migrationEventRepository.save(event);
        log.info("ExtractAndHashDelegate completed for docId: {}", docId);
    }
}
