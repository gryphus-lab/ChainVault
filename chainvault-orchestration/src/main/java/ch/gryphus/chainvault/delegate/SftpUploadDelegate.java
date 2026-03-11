/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
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
     * @param auditService     the audit service
     * @param migrationService the migration service
     */
    public SftpUploadDelegate(AuditEventService auditService, MigrationService migrationService) {
        super(auditService, "upload-sftp", "UPLOAD_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");
        String xml = (String) execution.getTransientVariable("xml");
        Path zipPath = (Path) execution.getTransientVariable("zipPath");
        Path pdfPath = (Path) execution.getTransientVariable("pdfPath");
        String processInstanceId = execution.getProcessInstanceId();
        migrationService.uploadToSftp(ctx, docId, xml, zipPath, pdfPath, processInstanceId);

        execution.setTransientVariable(
                "outputFileKey",
                "%s/%s-%s"
                        .formatted(
                                migrationService.getSftpTargetConfig().getRemoteDirectory(),
                                processInstanceId,
                                docId));

        Path workingDirectory = (Path) execution.getTransientVariable("workingDirectory");
        String zipPathRef =
                zipPath.toString().replaceAll("%s/".formatted(workingDirectory.toString()), "");
        execution.setTransientVariable("chainOfCustodyZip", zipPathRef);

        // cleanup temporary working directory
        FileSystemUtils.deleteRecursively(workingDirectory);
        log.info("Deleted working directory {}", workingDirectory);
    }
}
