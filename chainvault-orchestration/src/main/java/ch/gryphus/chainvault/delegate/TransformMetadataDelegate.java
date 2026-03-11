/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.service.MigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Transform metadata delegate.
 */
@Slf4j
@Component("transformMetadata")
@RequiredArgsConstructor
public class TransformMetadataDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;
    private final MigrationExecutor executor;

    @Override
    public void doExecute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "transform-metadata",
                "ASSEMBLY_FAILED",
                (span, docId, map) -> {
                    MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");
                    SourceMetadata meta = (SourceMetadata) execution.getTransientVariable("meta");

                    String xml = migrationService.transformMetadataToXml(meta, ctx);
                    execution.setTransientVariable("xml", xml);
                });
    }
}
