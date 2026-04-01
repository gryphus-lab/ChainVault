/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.model.dto;

import lombok.Data;

/**
 * The type Migration stats.
 */
@Data
public class MigrationStats {
    private long total;
    private int pending;
    private int running;
    private int success;
    private int failed;
    private int last24h;
}
