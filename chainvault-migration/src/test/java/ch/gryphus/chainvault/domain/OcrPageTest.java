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

/**
 * The type Tiff page test.
 */
class OcrPageTest {

    private OcrPage ocrPageUnderTest;

    /**
     * Sets up.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        ocrPageUnderTest =
                new OcrPage(
                        "sample1.tiff",
                        Files.readAllBytes(Paths.get("src/test/resources/tiffs/sample1.tiff")));
    }

    /**
     * Test equals returns false if same filename with different content.
     */
    @Test
    void testEqualsReturnsFalseIfSameFilenameWithDifferentContent() {
        assertThat(
                        ocrPageUnderTest.equals(
                                new OcrPage(
                                        "sample1.tiff", // filename same, different content
                                        "not_the_same_content".getBytes(StandardCharsets.UTF_8))))
                .isFalse();
    }

    /**
     * Test equals returns false for same content with different filename.
     *
     * @throws Exception the exception
     */
    @Test
    void testEqualsReturnsFalseForSameContentWithDifferentFilename() throws Exception {
        OcrPage anotherOcrPage =
                new OcrPage(
                        "not_sample1.tiff", // different filename, same content
                        Files.readAllBytes(Paths.get("src/test/resources/tiffs/not_sample1.tiff")));

        assertThat(ocrPageUnderTest.equals(anotherOcrPage)).isFalse();
    }

    /**
     * Test hash code returns non zero value.
     */
    @Test
    void testHashCodeReturnsNonZeroValue() {
        assertThat(ocrPageUnderTest.hashCode()).isNotZero();
    }

    /**
     * Test to string returns expected string.
     */
    @Test
    void testToStringReturnsExpectedString() {
        assertThat(ocrPageUnderTest).hasToString("OcrPage{name=sample1.tiff, mimeType=image/tiff}");
    }

    /**
     * Test name returns expected string.
     */
    @Test
    void testNameReturnsExpectedString() {
        assertThat(ocrPageUnderTest.getName()).isEqualTo("sample1.tiff");
    }
}
