/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The type Hash utils test.
 */
class HashUtilsTest {

    /**
     * The Temp dir.
     */
    @TempDir Path tempDir;

    /**
     * Test sha 2561 with file paths.
     *
     * @throws Exception the exception
     */
    @Test
    void testSha2561withFilePaths() throws Exception {
        assertThat(HashUtils.sha256(Path.of("src/test/resources/tiffs/sample1.tiff")))
                .isEqualTo("b6d36032c5a5d291a5d67ccdfdf09a5a90dedb29c50d0206660e453f77ffb8c5");

        assertThatThrownBy(() -> HashUtils.sha256(Path.of("this_file_does_not_exist.txt")))
                .isInstanceOf(IOException.class);
    }

    /**
     * Test sha 2562 with byte array.
     *
     * @throws Exception the exception
     */
    @Test
    void testSha2562withByteArray() throws Exception {
        assertThat(HashUtils.sha256("content".getBytes()))
                .isEqualTo("ed7002b439e9ac845f22357d822bac1444730fbdb6016d3ec9432297b9ec9f73");
    }

    /**
     * Sha 256 path should match byte array.
     *
     * @throws Exception the exception
     */
    @Test
    void sha256_path_shouldMatchByteArray() throws Exception {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello world");

        String hashFromBytes = HashUtils.sha256("hello world".getBytes());
        String hashFromPath = HashUtils.sha256(file);

        assertThat(hashFromPath).isEqualTo(hashFromBytes);
    }
}
