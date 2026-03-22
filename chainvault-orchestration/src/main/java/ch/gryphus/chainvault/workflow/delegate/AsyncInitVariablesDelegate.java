/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import ch.gryphus.chainvault.workflow.service.AuditEventService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Async init variables delegate.
 */
@Slf4j
@Component("asyncInitVars")
public class AsyncInitVariablesDelegate extends AbstractTracingDelegate {

    /**
     * Instantiates a new Async init variables delegate.
     *
     * @param openTelemetry the open telemetry
     * @param auditService  the audit service
     */
    public AsyncInitVariablesDelegate(OpenTelemetry openTelemetry, AuditEventService auditService) {
        super(openTelemetry, auditService, "async-init-vars", "ASYNC-INIT_FAILED");
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId) {
        log.info("async-init-vars executed for docId {}", docId);
    }
}
