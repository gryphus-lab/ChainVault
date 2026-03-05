/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

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

    private final MigrationExecutor executor;

    @Override
    public void execute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "init-variables",
                "INIT_FAILED",
                (span, docId) -> log.info("Init variables executed for docId: {}", docId));
    }
}
