/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
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
 * The type Sign document delegate.
 */
@Slf4j
@Component("signDocument")
public class SignDocumentDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;

    /**
     * Instantiates a new Sign document delegate.
     *
     * @param openTelemetry    the open telemetry
     * @param auditService     the audit service
     * @param migrationService the migration service
     */
    public SignDocumentDelegate(
            OpenTelemetry openTelemetry,
            AuditEventService auditService,
            MigrationService migrationService) {
        super(openTelemetry, auditService, "sign-document", "SIGN_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        byte[] payload = (byte[]) execution.getTransientVariable("payload");
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

        Path workingDirectory = (Path) execution.getTransientVariable("workingDirectory");
        List<TiffPage> pages =
                migrationService.signTiffPages(payload, ctx, workingDirectory.toString());
        execution.setTransientVariable("pages", pages);
    }
}
