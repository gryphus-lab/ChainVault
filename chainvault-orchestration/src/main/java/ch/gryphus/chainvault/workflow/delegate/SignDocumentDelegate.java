/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.OcrPage;
import ch.gryphus.chainvault.service.MigrationService;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import ch.gryphus.chainvault.workflow.service.SseEmitterService;
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
            SseEmitterService sseEmitterService,
            MigrationService migrationService) {
        super(openTelemetry, auditService, sseEmitterService, "sign-document", "SIGN_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        var payload = getTransientVariableSafely(execution, "payload", byte[].class);
        var migrationContext =
                Objects.requireNonNull(
                        getTransientVariableSafely(
                                execution, "migrationContext", MigrationContext.class));
        var workingDirectory =
                getTransientVariableSafely(execution, "workingDirectory", Path.class);

        List<OcrPage> pages =
                migrationService.signSourcePayload(payload, migrationContext, workingDirectory);

        execution.setTransientVariable("pages", pages);
    }
}
