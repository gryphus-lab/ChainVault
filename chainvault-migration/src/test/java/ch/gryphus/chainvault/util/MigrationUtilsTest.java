/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.jupiter.api.Test;

/**
 * The type Migration utils test.
 */
class MigrationUtilsTest {

    /**
     * Test get detected mime type.
     *
     * @throws Exception the exception
     */
    @Test
    void testGetDetectedMimeType() throws Exception {
        // Setup
        InputStream in = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));

        // Run the test
        String result = MigrationUtils.getDetectedMimeType(in);

        // Verify the results
        assertThat(result).isEqualTo("text/plain");
    }

    /**
     * Test get detected mime type empty in.
     *
     * @throws Exception the exception
     */
    @Test
    void testGetDetectedMimeType_EmptyIn() throws Exception {
        // Setup
        InputStream in = InputStream.nullInputStream();

        // Run the test
        String result = MigrationUtils.getDetectedMimeType(in);

        // Verify the results
        assertThat(result).isEqualTo("application/octet-stream");
    }

    /**
     * Test get detected mime type broken in.
     */
    @Test
    void testGetDetectedMimeType_BrokenIn() {
        // Setup
        InputStream in = new BrokenInputStream();

        // Run the test
        assertThatThrownBy(() -> MigrationUtils.getDetectedMimeType(in))
                .isInstanceOf(IOException.class);
    }
}
