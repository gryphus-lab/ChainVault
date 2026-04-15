/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The type Migration page.
 */
@Data
@AllArgsConstructor
public class MigrationPage {
    private List<Migration> items;
    private long total;
}