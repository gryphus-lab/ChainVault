/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Extract and hash delegate.
 */
@Slf4j
@Component("extractAndHash")
@RequiredArgsConstructor
public class ExtractAndHashDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;
    private final AuditEventService auditEventService;

    @Override
    protected AuditEventService getAuditEventService() {
        return auditEventService;
    }

    @Override
    protected String getTaskType() {
        return "extract-hash";
    }

    @Override
    protected String getErrorCode() {
        return "EXTRACTION_FAILED";
    }

    @Override
    public void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        Path path =
                Paths.get(
                        "%s-%s"
                                .formatted(
                                        migrationService.getTempDir(),
                                        execution.getProcessInstanceId()));
        Files.createDirectory(path);
        log.info("Created directory: {}", path);
        execution.setTransientVariable("workingDirectory", path);

        Map<String, Object> map1 = migrationService.extractAndHash(docId);

        execution.setTransientVariable("ctx", map1.get("ctx"));
        execution.setTransientVariable("meta", map1.get("meta"));
        execution.setTransientVariable("payload", map1.get("payload"));
    }
}
