/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.service.MigrationService;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Sftp upload delegate.
 */
@Slf4j
@Component("uploadSftp")
@RequiredArgsConstructor
public class SftpUploadDelegate implements JavaDelegate {
    private final MigrationService migrationService;

    @Override
    public void execute(DelegateExecution execution) {
        String docId = (String) execution.getVariable("docId");

        log.info("SftpUploadDelegate started for docId: {}", docId);

        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");
        String xml = (String) execution.getTransientVariable("xml");
        Path zipPath = (Path) execution.getTransientVariable("zipPath");
        Path pdfPath = (Path) execution.getTransientVariable("pdfPath");

        migrationService.uploadToSftp(ctx, docId, xml, zipPath, pdfPath);

        log.info("SftpUploadDelegate completed for docId: {}", docId);
    }
}
