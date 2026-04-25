/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Validates the structure and correctness of {@code src/main/resources/mapping-config.yml}.
 *
 * <p>This PR reformatted inline comments (trailing spaces removed) and fixed the missing newline
 * at end-of-file. These tests verify the semantic content remains correct.
 */
class MappingConfigTest {

    private static Map<String, Object> config;

    @BeforeAll
    static void loadConfig() throws Exception {
        try (InputStream is =
                MappingConfigTest.class
                        .getClassLoader()
                        .getResourceAsStream("mapping-config.yml")) {
            assertThat(is).as("mapping-config.yml must exist on the main classpath").isNotNull();
            Yaml yaml = new Yaml();
            config = yaml.load(is);
            assertThat(config)
                    .as("mapping-config.yml must not be empty or contain only whitespace")
                    .isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Top-level structure tests
    // -----------------------------------------------------------------------

    @Test
    void mappingConfig_ShouldHaveMappingsKey() {
        assertThat(config).containsKey("mappings");
    }

    @Test
    void mappingConfig_ShouldHaveEnrichmentKey() {
        assertThat(config).containsKey("enrichment");
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getMappings() {
        return (List<Map<String, Object>>) config.get("mappings");
    }

    private static Map<String, Object> getMapping(int i) {
        return getMappings().get(i);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getEnrichment() {
        return (Map<String, Object>) config.get("enrichment");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getMigration() {
        return (Map<String, Object>) getEnrichment().get("migration");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getIntegrity() {
        return (Map<String, Object>) getEnrichment().get("integrity");
    }

    private static byte[] readMappingConfigBytes() throws IOException {
        try (InputStream is =
                MappingConfigTest.class
                        .getClassLoader()
                        .getResourceAsStream("mapping-config.yml")) {
            assertThat(is).isNotNull();
            return is.readAllBytes();
        }
    }

    // -----------------------------------------------------------------------
    // mappings[] tests
    // -----------------------------------------------------------------------

    @Test
    void mappings_ShouldContainExactlyTwoEntries() {
        assertThat(getMappings()).hasSize(2);
    }

    @Test
    void mappings_FirstEntryShouldMapTitleToTitle() {
        Map<String, Object> first = getMapping(0);
        assertThat(first).containsEntry("source", "title").containsEntry("target", "title");
    }

    @Test
    void mappings_FirstEntryShouldHaveDcElementsNamespace() {
        Map<String, Object> first = getMapping(0);
        assertThat(first).containsEntry("namespace", "http://purl.org/dc/elements/1.1/");
    }

    @Test
    void mappings_SecondEntryShouldMapCreationDateToCreated() {
        Map<String, Object> second = getMapping(1);
        assertThat(second)
                .containsEntry("source", "creationDate")
                .containsEntry("target", "created");
    }

    @Test
    void mappings_SecondEntryShouldHaveDcTermsNamespace() {
        Map<String, Object> second = getMapping(1);
        assertThat(second).containsEntry("namespace", "http://purl.org/dc/terms/");
    }

    @Test
    void mappings_SecondEntryShouldHaveToIso8601Transform() {
        Map<String, Object> second = getMapping(1);
        assertThat(second).containsEntry("transform", "toIso8601");
    }

    @Test
    void mappings_FirstEntryShouldNotHaveTransform() {
        Map<String, Object> first = getMapping(0);
        assertThat(first).doesNotContainKey("transform");
    }

    // -----------------------------------------------------------------------
    // enrichment.migration tests
    // -----------------------------------------------------------------------

    @Test
    void enrichment_ShouldHaveMigrationSection() {
        assertThat(getEnrichment()).containsKey("migration");
    }

    @Test
    void enrichment_MigrationEventTypeShouldBeMigration() {
        assertThat(getMigration()).containsEntry("eventType", "Migration");
    }

    @Test
    void enrichment_MigrationToolShouldBeSwissArchiveMigrator() {
        assertThat(getMigration()).containsEntry("tool", "SwissArchiveMigrator v1.0");
    }

    @Test
    void enrichment_MigrationEventDateTimeShouldBeNowPlaceholder() {
        assertThat(getMigration()).containsEntry("eventDateTime", "${now}");
    }

    // -----------------------------------------------------------------------
    // enrichment.integrity tests
    // -----------------------------------------------------------------------

    @Test
    void enrichment_ShouldHaveIntegritySection() {
        assertThat(getEnrichment()).containsKey("integrity");
    }

    @Test
    void enrichment_IntegrityAlgorithmShouldBeSha256() {
        // PR fixed missing newline at EOF; value must remain "SHA-256"
        assertThat(getIntegrity()).containsEntry("algorithm", "SHA-256");
    }

    // -----------------------------------------------------------------------
    // File format / end-of-file tests
    // -----------------------------------------------------------------------

    @Test
    void mappingConfigFile_ShouldNotBeEmpty() throws IOException {
        byte[] bytes = readMappingConfigBytes();
        assertThat(bytes).isNotEmpty();
        String content = new String(bytes, StandardCharsets.UTF_8);
        assertThat(content.strip()).isNotEmpty();
    }

    @Test
    void mappingConfigFile_ShouldEndWithNewline() throws IOException {
        // PR added a trailing newline to mapping-config.yml
        byte[] bytes = readMappingConfigBytes();
        String content = new String(bytes, StandardCharsets.UTF_8);
        assertThat(content).endsWith("\n");
    }
}
