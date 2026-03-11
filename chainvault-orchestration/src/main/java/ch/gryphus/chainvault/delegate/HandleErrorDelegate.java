/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Handle error delegate.
 */
@Slf4j
@Component("handleError")
public class HandleErrorDelegate extends AbstractTracingDelegate {

    /**
     * Instantiates a new Handle error delegate.
     *
     * @param auditService the audit service
     */
    public HandleErrorDelegate(AuditEventService auditService) {
        super(auditService, "handle-error", "");
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        log.info("HandleErrorDelegate executed for {}", execution.getProcessInstanceId());
    }
}
