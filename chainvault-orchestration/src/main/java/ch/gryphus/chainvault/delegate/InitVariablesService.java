/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import io.opentelemetry.api.trace.Span;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Init variables service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitVariablesService implements JavaDelegate {

    private final MigrationAuditRepository auditRepo;

    @Override
    public void execute(DelegateExecution execution) {
        String docId = (String) execution.getVariable("docId");
        log.info("Initialize variables started for docId:{}", docId);

        String piKey = execution.getProcessInstanceId();

        // Update audit
        MigrationAudit audit =
                auditRepo
                        .findByProcessInstanceKey(piKey)
                        .orElseThrow(() -> new IllegalStateException("No audit for " + piKey));
        audit.setStatus(MigrationAudit.MigrationStatus.RUNNING);
        audit.setCompletedAt(Instant.now());
        audit.setTraceId(Span.current().getSpanContext().getTraceId());

        auditRepo.save(audit);

        log.info("Initialize variables completed for docId:{}", docId);
    }
}
