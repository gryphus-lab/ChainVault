/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The type Serialization config test.
 */
class SerializationConfigTest {

    private SerializationConfig serializationConfigUnderTest;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        serializationConfigUnderTest = new SerializationConfig();
    }

    /**
     * Test object mapper.
     */
    @Test
    void testObjectMapper() {
        // Setup
        // Run the test
        var result = serializationConfigUnderTest.objectMapper();

        // Verify the results
        assertThat(result).isNotNull();
    }

    /**
     * Test json mapper.
     */
    @Test
    void testJsonMapper() {
        // Setup
        // Run the test
        var result = serializationConfigUnderTest.jsonMapper();

        // Verify the results
        assertThat(result).isNotNull();
    }

    /**
     * Test xml mapper.
     */
    @Test
    void testXmlMapper() {
        // Setup
        // Run the test
        var result = serializationConfigUnderTest.xmlMapper();

        // Verify the results
        assertThat(result).isNotNull();
    }

    /**
     * Test tika.
     */
    @Test
    void testTika() {
        // Setup
        // Run the test
        var result = serializationConfigUnderTest.tika();

        // Verify the results
        assertThat(result).isNotNull();
    }
}
