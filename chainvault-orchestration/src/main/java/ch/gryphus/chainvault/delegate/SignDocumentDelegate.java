/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.MigrationService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Sign document delegate.
 */
@Slf4j
@Component("signDocument")
@RequiredArgsConstructor
public class SignDocumentDelegate implements JavaDelegate {

    private final MigrationService migrationService;

    @Override
    public void execute(DelegateExecution execution) {
        String docId = (String) execution.getVariable("docId");
        log.info("SignDocumentDelegate started for docId: {}", docId);

        byte[] payload = (byte[]) execution.getTransientVariable("payload");
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

        List<TiffPage> pages;
        try {
            pages = migrationService.signTiffPages(payload, ctx);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("error signing TIFF pages", e);
        }
        execution.setTransientVariable("pages", pages);

        log.info("SignDocumentDelegate completed for docId: {}", docId);
    }
}
