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
 * Migration Properties
 *
 * @param tempDir the temp dir
 * @param zipThresholdSize the zip threshold size
 * @param zipThresholdRatio the zip threshold ratio
 * @param zipThresholdEntries the zip threshold entries
 * @param tesseractLanguage the tesseract language
 * @param tesseractDpi the tesseract dpi
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
