/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import ch.gryphus.chainvault.service.MigrationServiceException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

/**
 * The type Hash utils.
 */
public class HashUtils {

    private HashUtils() {}

    /**
     * Sha 256 string.
     *
     * @param path the path
     * @return  the string
     * @throws IOException the io exception
     */
    public static String sha256(Path path) throws IOException {
        return sha256(Files.readAllBytes(path));
    }

    /**
     * Sha 256 string.
     *
     * @param data the data
     * @return the string
     */
    public static String sha256(byte[] data) {
        try {
            return Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new MigrationServiceException(e.getMessage());
        }
    }
}
