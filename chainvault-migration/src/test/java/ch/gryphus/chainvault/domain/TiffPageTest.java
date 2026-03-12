/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TiffPageTest {

    private TiffPage tiffPageUnderTest;

    @BeforeEach
    void setUp() {
        tiffPageUnderTest =
                new TiffPage("sample1.tiff", "content".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testEquals() {
        assertThat(
                        tiffPageUnderTest.equals(
                                new TiffPage(
                                        "sample1.tiff", // filename same, different content
                                        "not_the_same_content".getBytes(StandardCharsets.UTF_8))))
                .isFalse();
    }

    @Test
    void testHashCode() {
        assertThat(tiffPageUnderTest.hashCode()).isNotZero();
    }

    @Test
    void testToString() {
        assertThat(tiffPageUnderTest).hasToString("TiffPage{name=sample1.tiff}");
    }

    @Test
    void testName() {
        assertThat(tiffPageUnderTest.name()).isEqualTo("sample1.tiff");
    }

    @Test
    void testData() {
        assertThat(tiffPageUnderTest.data()).isEqualTo("content".getBytes(StandardCharsets.UTF_8));
    }
}
