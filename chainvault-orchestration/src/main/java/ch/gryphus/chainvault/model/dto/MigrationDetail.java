/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.model.dto;

import ch.gryphus.chainvault.model.entity.MigrationEvent;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Migration detail.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MigrationDetail extends Migration {
    private List<MigrationEvent> events;
    private String ocrTextPreview;
    private String chainZipUrl;
    private String pdfUrl;
}
