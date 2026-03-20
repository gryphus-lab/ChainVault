/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.OcrPage;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import ch.gryphus.chainvault.utils.HashUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Merge pdf delegate.
 */
@Slf4j
@Component("mergePdf")
public class MergePdfDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;

    /**
     * Instantiates a new Merge pdf delegate.
     *
     * @param openTelemetry    the open telemetry
     * @param auditService     the audit service
     * @param migrationService the migration service
     */
    public MergePdfDelegate(
            OpenTelemetry openTelemetry,
            AuditEventService auditService,
            MigrationService migrationService) {
        super(openTelemetry, auditService, "merge-pdfs", "MERGE_FAILED");
        this.migrationService = migrationService;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        List<OcrPage> pages = getTransientVariableSafely(execution, "pages", List.class);
        if (pages != null && !pages.isEmpty()) {
            var migrationContext =
                    Objects.requireNonNull(
                            getTransientVariableSafely(
                                    execution, "migrationContext", MigrationContext.class));

            var workingDirectory =
                    getTransientVariableSafely(execution, "workingDirectory", Path.class);
            Path pdfPath = migrationService.createMergedPdf(pages, docId, workingDirectory);
            migrationContext.setPdfHash(HashUtils.sha256(pdfPath));

            execution.setTransientVariable("migrationContext", migrationContext);
            execution.setTransientVariable("pdfPath", pdfPath);
        }
    }
}
