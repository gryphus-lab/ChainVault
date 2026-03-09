/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.MigrationService;
import java.nio.file.Path;
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
    private final MigrationExecutor executor;

    @Override
    public void execute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "sign-document",
                "SIGN_FAILED",
                (span, docId, map) -> {
                    byte[] payload = (byte[]) execution.getTransientVariable("payload");
                    MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

                    Path workingDirectory =
                            (Path) execution.getTransientVariable("workingDirectory");
                    List<TiffPage> pages =
                            migrationService.signTiffPages(
                                    payload, ctx, workingDirectory.toString());
                    execution.setTransientVariable("pages", pages);
                });
    }
}
