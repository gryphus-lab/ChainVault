/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Perform ocr delegate.
 */
@Slf4j
@Component("performOcr")
public class PerformOcrDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;

    /**
     * Instantiates a new Perform ocr delegate.
     *
     * @param openTelemetry    the open telemetry
     * @param auditService     the audit service
     * @param migrationService the migration service
     */
    public PerformOcrDelegate(
            OpenTelemetry openTelemetry,
            AuditEventService auditService,
            MigrationService migrationService) {
        super(openTelemetry, auditService, "perform-ocr", "OCR_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException, TesseractException {
        List<TiffPage> pages = (List<TiffPage>) execution.getTransientVariable("pages");
        if (pages != null && !pages.isEmpty()) {
            List<String> ocrResults = migrationService.performOcrOnTiffPages(pages);

            execution.setTransientVariable("ocrResults", ocrResults);
            execution.setTransientVariable(
                    "ocrTextLength", ocrResults.stream().mapToInt(String::length).sum());
            execution.setTransientVariable("ocrPageCount", ocrResults.size());
        } else {
            log.warn("No pages found for document {}", docId);
        }
    }
}
