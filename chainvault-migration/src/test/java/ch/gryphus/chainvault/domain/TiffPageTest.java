/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TiffPageTest {

    private TiffPage tiffPageUnderTest;

    @BeforeEach
    void setUp() throws Exception {
        tiffPageUnderTest =
                new TiffPage(
                        "sample1.tiff",
                        Files.readAllBytes(Paths.get("src/test/resources/tiffs/sample1.tiff")));
    }

    @Test
    void testEqualsReturnsFalseIfSameFilenameWithDifferentContent() {
        assertThat(
                        tiffPageUnderTest.equals(
                                new TiffPage(
                                        "sample1.tiff", // filename same, different content
                                        "not_the_same_content".getBytes(StandardCharsets.UTF_8))))
                .isFalse();
    }

    @Test
    void testEqualsReturnsFalseForSameContentWithDifferentFilename() throws Exception {
        TiffPage anotherTiffPage =
                new TiffPage(
                        "not_sample1.tiff", // different filename, same content
                        Files.readAllBytes(Paths.get("src/test/resources/tiffs/not_sample1.tiff")));

        assertThat(tiffPageUnderTest.equals(anotherTiffPage)).isFalse();
    }

    @Test
    void testHashCodeReturnsNonZeroValue() {
        assertThat(tiffPageUnderTest.hashCode()).isNotZero();
    }

    @Test
    void testToStringReturnsExpectedString() {
        assertThat(tiffPageUnderTest).hasToString("TiffPage{name=sample1.tiff}");
    }

    @Test
    void testNameReturnsExpectedString() {
        assertThat(tiffPageUnderTest.name()).isEqualTo("sample1.tiff");
    }
}
