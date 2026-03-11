/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.MigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Init variables service.
 */
@Slf4j
@Component("asyncInitVars")
@RequiredArgsConstructor
public class AsyncInitVariablesDelegate implements JavaDelegate {

    private final MigrationService migrationService;
    private final MigrationExecutor executor;

    @Override
    public void execute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "async-init-vars",
                null,
                (span, docId, map) -> {
                    // do nothing
                });
    }
}
