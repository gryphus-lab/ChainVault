/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Handle error delegate.
 */
@Slf4j
@Component("handleError")
@RequiredArgsConstructor
public class HandleErrorDelegate extends AbstractTracingDelegate {

    private final AuditEventService auditEventService;

    @Override
    protected AuditEventService getAuditEventService() {
        return auditEventService;
    }

    @Override
    protected String getTaskType() {
        return "handle-error";
    }

    @Override
    protected String getErrorCode() {
        return "";
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        log.info("HandleErrorDelegate executed for {}", execution.getProcessInstanceId());
    }
}
