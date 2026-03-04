/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.MigrationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** The type Extract and hash delegate. */
@Slf4j
@Component("extractAndHash")
@RequiredArgsConstructor
public class ExtractAndHashDelegate implements JavaDelegate {

    private final MigrationService migrationService;
    private final MigrationExecutor executor;

    @Override
    public void execute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "extract-hash",
                "EXTRACTION_FAILED",
                (span, docId) -> {
                    Map<String, Object> map;
                    map = migrationService.extractAndHash(docId);

                    execution.setTransientVariable("ctx", map.get("ctx"));
                    execution.setTransientVariable("meta", map.get("meta"));
                    execution.setTransientVariable("payload", map.get("payload"));
                });
    }
}
