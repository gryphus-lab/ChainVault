/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.NonNull;
import org.apache.commons.codec.binary.Hex;

/**
 * The type Hash utils.
 */
public final class HashUtils {

    private HashUtils() {
        // empty constructor
    }

    /**
     * Sha 256 string.
     *
     * @param path the path
     * @return the string
     * @throws IOException              the io exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static @NonNull String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        return sha256(Files.readAllBytes(path));
    }

    /**
     * Sha 256 string.
     *
     * @param data the data
     * @return the string
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static @NonNull String sha256(byte[] data) throws NoSuchAlgorithmException {
        return Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(data));
    }
}
