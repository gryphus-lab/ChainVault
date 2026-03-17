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
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Prepare files delegate.
 */
@Slf4j
@Component("prepareFiles")
public class PrepareFilesDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;

    /**
     * Instantiates a new Prepare files delegate.
     *
     * @param openTelemetry    the open telemetry
     * @param auditService     the audit service
     * @param migrationService the migration service
     */
    protected PrepareFilesDelegate(
            OpenTelemetry openTelemetry,
            AuditEventService auditService,
            MigrationService migrationService) {
        super(openTelemetry, auditService, "prepare-files", "PREPARE_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        List<TiffPage> pages = getTransientVariableSafely(execution, "pages", ArrayList.class);
        var meta = getTransientVariableSafely(execution, "meta", SourceMetadata.class);
        var migrationContext =
                Objects.requireNonNull(
                        getTransientVariableSafely(
                                execution, "migrationContext", MigrationContext.class));

        var workingDirectory =
                getTransientVariableSafely(execution, "workingDirectory", Path.class);

        Path zipPath =
                migrationService.createChainZip(
                        docId,
                        pages,
                        Objects.requireNonNull(meta),
                        migrationContext,
                        workingDirectory);
        migrationContext.setZipHash(HashUtils.sha256(zipPath));

        execution.setTransientVariable("migrationContext", migrationContext);
        execution.setTransientVariable("zipPath", zipPath);
    }
}
