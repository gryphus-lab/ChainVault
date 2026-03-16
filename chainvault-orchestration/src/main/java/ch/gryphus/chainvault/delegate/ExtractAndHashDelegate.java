/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
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
            MigrationService migrationService) {
        super(openTelemetry, auditService, "extract-hash", "EXTRACTION_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        Path path =
                Paths.get(
                        "%s-%s"
                                .formatted(
                                        migrationService.getTempDir(),
                                        execution.getProcessInstanceId()));
        if (Files.notExists(path)) {
            Files.createDirectory(path);
            log.info("Created directory: {}", path);
        } else {
            log.warn("Directory already exists: {}", path);
        }
        execution.setTransientVariable("workingDirectory", path);

        Map<String, Object> map = migrationService.extractAndHash(docId);

        execution.setTransientVariable("ctx", map.get("ctx"));
        execution.setTransientVariable("meta", map.get("meta"));
        execution.setTransientVariable("payload", map.get("payload"));
    }
}
