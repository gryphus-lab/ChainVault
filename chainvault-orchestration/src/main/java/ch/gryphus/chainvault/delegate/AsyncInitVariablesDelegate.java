/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.trace.Tracer;
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
    private final MigrationExecutor executor;
    private final Tracer tracer;

    @Override
    public void doExecute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "async-init-vars",
                "ASYNC-INIT_FAILED",
                (span, docId, map) -> log.info("async-init-vars executed for docId {}", docId));
    }
}
