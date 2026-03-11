/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.service.MigrationService;
import ch.gryphus.chainvault.utils.HashUtils;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Merge pdf delegate.
 */
@Slf4j
@Component("mergePdf")
@RequiredArgsConstructor
public class MergePdfDelegate extends AbstractTracingDelegate {
    private final MigrationService migrationService;
    private final MigrationExecutor executor;

    @SuppressWarnings("unchecked")
    @Override
    public void doExecute(DelegateExecution execution) {
        executor.executeStep(
                execution,
                "merge-pdfs",
                "ASSEMBLY_FAILED",
                (span, docId, map) -> {
                    List<TiffPage> pages = (List<TiffPage>) execution.getTransientVariable("pages");
                    MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");

                    Path workingDirectory =
                            (Path) execution.getTransientVariable("workingDirectory");
                    Path pdfPath =
                            migrationService.mergeTiffToPdf(
                                    pages, docId, workingDirectory.toString());
                    ctx.setPdfHash(HashUtils.sha256(pdfPath));

                    execution.setTransientVariable("ctx", ctx);
                    execution.setTransientVariable("pdfPath", pdfPath);
                });
    }
}
