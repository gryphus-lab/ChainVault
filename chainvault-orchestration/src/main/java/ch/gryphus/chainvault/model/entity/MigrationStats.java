/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.model.entity;

import lombok.Data;

@Data
public class MigrationStats {
    private long total;
    private int pending;
    private int running;
    private int success;
    private int failed;
    private int last24h;
}
