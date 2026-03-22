/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

/**
 * The type Sftp upload delegate.
 */
@Slf4j
@Component("uploadSftp")
public class SftpUploadDelegate extends AbstractTracingDelegate {
    private final MigrationService migrationService;

    /**
     * Instantiates a new Sftp upload delegate.
     *
     * @param openTelemetry    the open telemetry
     * @param auditService     the audit service
     * @param migrationService the migration service
     */
    public SftpUploadDelegate(
            OpenTelemetry openTelemetry,
            AuditEventService auditService,
            MigrationService migrationService) {
        super(openTelemetry, auditService, "upload-sftp", "UPLOAD_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {

        var xml = getTransientVariableSafely(execution, "xml", String.class);
        var zipPath =
                Objects.requireNonNull(
                        getTransientVariableSafely(execution, "zipPath", Path.class));
        var pdfPath = getTransientVariableSafely(execution, "pdfPath", Path.class);
        var workingDirectory =
                Objects.requireNonNull(
                        getTransientVariableSafely(execution, "workingDirectory", Path.class));
        var migrationContext =
                getTransientVariableSafely(execution, "migrationContext", MigrationContext.class);

        String processInstanceId = execution.getProcessInstanceId();
        String outputFileKey =
                migrationService.createSftpUploadTarget(
                        docId, xml, zipPath, pdfPath, processInstanceId, migrationContext);

        execution.setTransientVariable("outputFileKey", outputFileKey);

        String zipPathRef =
                zipPath.toString().replaceAll("%s/".formatted(workingDirectory.toString()), "");
        execution.setTransientVariable("chainOfCustodyZip", zipPathRef);

        // cleanup temporary working directory
        FileSystemUtils.deleteRecursively(workingDirectory);
        log.info("Deleted working directory {}", workingDirectory);
    }
}
