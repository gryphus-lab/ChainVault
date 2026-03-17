/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param tempDir
 * @param zipThresholdSize
 * @param zipThresholdRatio
 * @param zipThresholdEntries
 * @param tesseractLanguage
 * @param tesseractDpi
 */
@Validated
@ConfigurationProperties(prefix = "migration")
public record MigrationProperties(
        @NotBlank String tempDir,
        @Min(1) long zipThresholdSize,
        @Positive double zipThresholdRatio,
        @Min(10) int zipThresholdEntries,
        @NotBlank String tesseractLanguage,
        @Min(72) int tesseractDpi) {}
