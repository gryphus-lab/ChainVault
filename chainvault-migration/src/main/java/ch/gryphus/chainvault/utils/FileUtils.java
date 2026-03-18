/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.Tika;

/**
 * The type File utils.
 */
public final class FileUtils {
    private FileUtils() {
        // empty constructor
    }

    private static final Tika tika = new Tika();

    /**
     * Gets detected mime type.
     *
     * @param in the in
     * @return the detected mime type
     * @throws IOException the io exception
     */
    public static String getDetectedMimeType(InputStream in) throws IOException {
        return tika.detect(in);
    }

    /**
     * Gets detected mime type.
     *
     * @param bytes the bytes
     * @return the detected mime type
     */
    public static String getDetectedMimeType(byte[] bytes) {
        return tika.detect(bytes);
    }
}
