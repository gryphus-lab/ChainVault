/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

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
 * The type Extract and hash delegate.
 */
@Slf4j
@Component("performOcr")
public class PerformOcrDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;

    /**
     * Instantiates a new Extract and hash delegate.
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
    public void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException, TesseractException {

        @SuppressWarnings("unchecked")
        List<byte[]> tiffPages = (List<byte[]>) execution.getTransientVariable("pages");
        String workingDirectory = (String) execution.getTransientVariable("workingDirectory");

        if (tiffPages == null || tiffPages.isEmpty()) {
            throw new IllegalStateException("No TIFF pages found for OCR");
        }

        List<String> ocrResults =
                migrationService.performOcrOnTiffPages(tiffPages, workingDirectory);

        execution.setTransientVariable("ocrResults", ocrResults);
        execution.setTransientVariable(
                "ocrTextLength", ocrResults.stream().mapToInt(String::length).sum());
    }
}
