/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.service.MigrationService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;

/**
 * The type Transform metadata delegate.
 */
@Slf4j
@Component("transformMetadata")
public class TransformMetadataDelegate implements JavaDelegate {

    private final MigrationService migrationService;

    /**
     * Instantiates a new Transform metadata delegate.
     *
     * @param migrationService the migration service
     */
    public TransformMetadataDelegate(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String docId = (String) execution.getVariable("docId");

        log.info("TransformDocumentDelegate started for docId: {}", docId);

        MigrationContext ctx = (MigrationContext) execution.getTransientVariable("ctx");
        SourceMetadata meta = (SourceMetadata) execution.getTransientVariable("meta");
        String xml;
        try {
            xml = migrationService.transformMetadataToXml(meta, ctx);
        } catch (JacksonException e) {
            throw new IllegalStateException("failed to transform metadata to xml", e);
        }
        execution.setTransientVariable("xml", xml);

        log.info("TransformDocumentDelegate completed for docId: {}", docId);
    }
}
