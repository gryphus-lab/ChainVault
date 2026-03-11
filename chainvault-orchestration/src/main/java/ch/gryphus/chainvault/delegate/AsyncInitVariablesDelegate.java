/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Init variables service.
 */
@Slf4j
@Component("asyncInitVars")
@RequiredArgsConstructor
public class AsyncInitVariablesDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;
    private final AuditEventService auditEventService;

    @Override
    protected AuditEventService getAuditEventService() {
        return auditEventService;
    }

    @Override
    protected String getTaskType() {
        return "async-init-vars";
    }

    @Override
    protected String getErrorCode() {
        return "ASYNC-INIT_FAILED";
    }

    @Override
    public void doExecute(DelegateExecution execution, Span span, String docId) {
        log.info("async-init-vars executed for docId {}", docId);
    }
}
