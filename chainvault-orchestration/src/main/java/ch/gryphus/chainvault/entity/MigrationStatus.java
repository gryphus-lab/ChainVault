/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.entity;

public enum MigrationStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED,
    RETRYING,
    COMPENSATED
}
