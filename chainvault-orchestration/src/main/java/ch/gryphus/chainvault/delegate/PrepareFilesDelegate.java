/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import ch.gryphus.chainvault.utils.HashUtils;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Prepare files delegate.
 */
@Slf4j
@Component("prepareFiles")
@RequiredArgsConstructor
public class PrepareFilesDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;
    private final AuditEventService auditEventService;

    @Override
    protected AuditEventService getAuditEventService() {
        return auditEventService;
    }

    @Override
    protected String getTaskType() {
        return "prepare-files";
    }

    @Override
    protected String getErrorCode() {
        return "PREPARE_FAILED";
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        var pages = (List<TiffPage>) execution.getTransientVariable("pages");
        SourceMetadata meta = (SourceMetadata) execution.getTransientVariable("meta");
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

        Path workingDirectory = (Path) execution.getTransientVariable("workingDirectory");
        Path zipPath =
                migrationService.createChainZip(
                        docId, pages, meta, ctx, workingDirectory.toString());
        ctx.setZipHash(HashUtils.sha256(zipPath));

        execution.setTransientVariable("ctx", ctx);
        execution.setTransientVariable("zipPath", zipPath);
    }
}
