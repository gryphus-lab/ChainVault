/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.MigrationService;
import ch.gryphus.chainvault.utils.HashUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Merge pdf delegate.
 */
@Slf4j
@Component("mergePdf")
@RequiredArgsConstructor
public class MergePdfDelegate implements JavaDelegate {
    private final MigrationService migrationService;

    @Override
    public void execute(DelegateExecution execution) {
        String docId = (String) execution.getVariable("docId");
        log.info("MergePdfDelegate started for docId:{}", docId);

        List<TiffPage> pages = (List<TiffPage>) execution.getTransientVariable("pages");
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

        Path pdfPath;
        try {
            pdfPath = migrationService.mergeTiffToPdf(pages, docId);
            ctx.setPdfHash(HashUtils.sha256(pdfPath));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("error preparing PDF or computing hash", e);
        }

        execution.setTransientVariable("ctx", ctx);
        execution.setTransientVariable("pdfPath", pdfPath);

        log.info("MergePdfDelegate completed for docId:{}", docId);
    }
}
