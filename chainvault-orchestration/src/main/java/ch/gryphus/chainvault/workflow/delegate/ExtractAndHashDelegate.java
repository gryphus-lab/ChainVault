/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import ch.gryphus.chainvault.service.MigrationService;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import ch.gryphus.chainvault.workflow.service.SseEmitterService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Extract and hash delegate.
 */
@Slf4j
@Component("extractAndHash")
public class ExtractAndHashDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;

    /**
     * Instantiates a new Extract and hash delegate.
     *
     * @param openTelemetry    the open telemetry
     * @param auditService     the audit service
     * @param migrationService the migration service
     */
    public ExtractAndHashDelegate(
            OpenTelemetry openTelemetry,
            AuditEventService auditService,
            SseEmitterService sseEmitterService,
            MigrationService migrationService) {
        super(openTelemetry, auditService, sseEmitterService, "extract-hash", "EXTRACTION_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        Path workingDirectory =
                Paths.get(
                        "%s-%s"
                                .formatted(
                                        migrationService.getTempDir(),
                                        execution.getProcessInstanceId()));
        if (Files.notExists(workingDirectory)) {
            Files.createDirectory(workingDirectory);
            log.info("Created working directory: {}", workingDirectory);
        } else {
            log.warn("Working directory already exists: {}", workingDirectory);
        }
        execution.setTransientVariable("workingDirectory", workingDirectory);

        Map<String, Object> map = migrationService.extractAndHash(docId);
        execution.setTransientVariable("migrationContext", map.get("migrationContext"));
        execution.setTransientVariable("meta", map.get("meta"));
        execution.setTransientVariable("payload", map.get("payload"));
    }
}
