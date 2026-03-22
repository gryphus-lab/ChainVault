/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import ch.gryphus.chainvault.workflow.service.AuditEventService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

/**
 * The type Handle error delegate.
 */
@Slf4j
@Component("handleError")
public class HandleErrorDelegate extends AbstractTracingDelegate {

    /**
     * Instantiates a new Handle error delegate.
     *
     * @param openTelemetry the open telemetry
     * @param auditService  the audit service
     */
    public HandleErrorDelegate(OpenTelemetry openTelemetry, AuditEventService auditService) {
        super(openTelemetry, auditService, "handle-error", "");
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        // cleanup temporary working directory
        var workingDirectory =
                getTransientVariableSafely(execution, "workingDirectory", Path.class);

        FileSystemUtils.deleteRecursively(workingDirectory);
        log.info("Deleted working directory {}", workingDirectory);
    }
}
