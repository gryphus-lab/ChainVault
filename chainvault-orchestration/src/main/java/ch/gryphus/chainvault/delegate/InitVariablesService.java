/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.trace.Span;
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

    private final AuditEventService auditEventService;

    @Override
    public void execute(DelegateExecution execution) {
        Span span = Span.current();
        String docId = (String) execution.getVariable("docId");
        span.setAttribute("document.id", docId);
        log.info("Initialize variables started for docId:{}", docId);

        String piKey = execution.getProcessInstanceId();
        String eventTaskType = "init-variables";
        auditEventService.updateAuditEventStart(piKey, docId, eventTaskType);

        // Update audit
        auditEventService.updateAuditEventEnd(
                piKey,
                MigrationAudit.MigrationStatus.RUNNING,
                null,
                null,
                eventTaskType,
                "Initialize variables completed");

        log.info("Initialize variables completed for docId:{}", docId);
    }
}
