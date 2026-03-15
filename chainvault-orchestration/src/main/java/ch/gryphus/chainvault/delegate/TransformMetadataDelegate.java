/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Transform metadata delegate.
 */
@Slf4j
@Component("transformMetadata")
public class TransformMetadataDelegate extends AbstractTracingDelegate {

    private final MigrationService migrationService;

    /**
     * Instantiates a new Transform metadata delegate.
     *
     * @param openTelemetry    the open telemetry
     * @param auditService     the audit service
     * @param migrationService the migration service
     */
    public TransformMetadataDelegate(
            OpenTelemetry openTelemetry,
            AuditEventService auditService,
            MigrationService migrationService) {
        super(openTelemetry, auditService, "transform-metadata", "TRANSFORM_FAILED");
        this.migrationService = migrationService;
    }

    @Override
    protected void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException {
        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");
        SourceMetadata meta = (SourceMetadata) execution.getTransientVariable("meta");

        Map<String, Object> map = new HashMap<>();
        map.put("ocrResults", execution.getTransientVariable(("ocrResults")));
        map.put("ocrTextLength", execution.getTransientVariable(("ocrTextLength")));
        map.put("ocrPageCount", execution.getTransientVariable(("ocrPageCount")));

        String xml = migrationService.transformMetadataToXml(meta, ctx, map);
        execution.setTransientVariable("xml", xml);
    }
}
