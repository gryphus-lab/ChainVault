/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import ch.gryphus.chainvault.utils.HashUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Merge pdf delegate.
 */
@Slf4j
@Component("mergePdf")
public class MergePdfDelegate extends AbstractTracingDelegate {

    /**
     * Instantiates a new Merge pdf delegate.
     *
     * @param openTelemetry the open telemetry
     * @param auditService  the audit service
     */
    public MergePdfDelegate(OpenTelemetry openTelemetry, AuditEventService auditService) {
        super(openTelemetry, auditService, "merge-pdfs", "MERGE_FAILED");
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        List<TiffPage> pages = (List<TiffPage>) execution.getTransientVariable("pages");
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

        Path workingDirectory = (Path) execution.getTransientVariable("workingDirectory");
        Path pdfPath = MigrationService.mergeTiffToPdf(pages, docId, workingDirectory.toString());
        ctx.setPdfHash(HashUtils.sha256(pdfPath));

        execution.setTransientVariable("ctx", ctx);
        execution.setTransientVariable("pdfPath", pdfPath);
    }
}
