/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.MigrationService;
import ch.gryphus.chainvault.utils.HashUtils;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Prepare files delegate.
 */
@Slf4j
@Component("prepareFiles")
@RequiredArgsConstructor
public class PrepareFilesDelegate implements JavaDelegate {

    private final MigrationService migrationService;
    private final MigrationExecutor executor;

    @SuppressWarnings("unchecked")
    @Override
    public void execute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "prepare-files",
                "ASSEMBLY_FAILED",
                (span, docId) -> {
                    var pages = (List<TiffPage>) execution.getTransientVariable("pages");
                    SourceMetadata meta = (SourceMetadata) execution.getTransientVariable("meta");
                    MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

                    Path zipPath = migrationService.createChainZip(docId, pages, meta, ctx);
                    ctx.setZipHash(HashUtils.sha256(zipPath));

                    execution.setTransientVariable("ctx", ctx);
                    execution.setTransientVariable("zipPath", zipPath);
                });
    }
}
