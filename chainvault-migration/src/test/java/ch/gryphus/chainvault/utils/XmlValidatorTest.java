/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XmlValidatorTest {
    @BeforeEach
    void setUp() {
        XmlValidator.setXsdPath("src/test/resources/xmls/ArchivalMetadata.xsd");
    }

    @SneakyThrows
    @Test
    void testValidXML() {
        assertThat(
                        XmlValidator.isValid(
                                Files.readString(
                                        Path.of("src/test/resources/xmls/valid-test.xml"))))
                .isTrue();
    }

    @SneakyThrows
    @Test
    void testInvalidXML() {
        assertThat(
                        XmlValidator.isValid(
                                Files.readString(
                                        Path.of("src/test/resources/xmls/invalid-test.xml"))))
                .isFalse();
    }

    @Test
    void testSetXsdPath() {
        // Verify the results
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            XmlValidator.setXsdPath("src/test/resources/xmls/ArchivalMetadata.xsd");
                        });
    }
}
