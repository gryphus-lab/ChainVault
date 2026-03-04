/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.MigrationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** The type Sign document delegate. */
@Slf4j
@Component("signDocument")
@RequiredArgsConstructor
public class SignDocumentDelegate implements JavaDelegate {

    private final MigrationService migrationService;
    private final MigrationExecutor executor;

    @Override
    public void execute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "sign-document",
                "SIGN_FAILED",
                (span, docId) -> {
                    byte[] payload = (byte[]) execution.getTransientVariable("payload");
                    MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

                    List<TiffPage> pages;
                    pages = migrationService.signTiffPages(payload, ctx);
                    execution.setTransientVariable("pages", pages);
                });
    }
}
