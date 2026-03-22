/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The type Xml validator test.
 */
class XmlValidationUtilsTest {
    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        XmlValidationUtils.setXsdPath("src/test/resources/xmls/ArchivalMetadata.xsd");
    }

    /**
     * Test valid xml.
     */
    @Test
    void testValidXML() throws IOException {
        assertThat(
                        XmlValidationUtils.isValid(
                                Files.readString(
                                        Path.of("src/test/resources/xmls/valid-test.xml"))))
                .isTrue();
    }

    /**
     * Test invalid xml.
     */
    @Test
    void testInvalidXML() throws IOException {
        assertThat(
                        XmlValidationUtils.isValid(
                                Files.readString(
                                        Path.of("src/test/resources/xmls/invalid-test.xml"))))
                .isFalse();
    }

    /**
     * Test set xsd path.
     */
    @Test
    void testSetXsdPath() {
        // Verify the results
        assertThatNoException()
                .isThrownBy(
                        () ->
                                XmlValidationUtils.setXsdPath(
                                        "src/test/resources/xmls/ArchivalMetadata.xsd"));
    }
}
