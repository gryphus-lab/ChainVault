/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Transform metadata delegate.
 */
@Slf4j
@Component("transformMetadata")
@RequiredArgsConstructor
public class TransformMetadataDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;
    private final AuditEventService auditEventService;

    @Override
    protected AuditEventService getAuditEventService() {
        return auditEventService;
    }

    @Override
    protected String getTaskType() {
        return "transform-metadata";
    }

    @Override
    protected String getErrorCode() {
        return "TRANSFORM_FAILED";
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");
        SourceMetadata meta = (SourceMetadata) execution.getTransientVariable("meta");

        String xml = migrationService.transformMetadataToXml(meta, ctx);
        execution.setTransientVariable("xml", xml);
    }
}
