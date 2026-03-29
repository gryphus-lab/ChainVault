/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.model.entity;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MigrationDetail extends Migration {
    private List<MigrationEvent> events;
    private String ocrTextPreview;
    private String chainZipUrl;
    private String pdfUrl;
}
