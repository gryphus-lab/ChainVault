/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * The type Migration service exception.
 */
public class MigrationServiceException extends RuntimeException {
    /**
     * Instantiates a new Migration service exception.
     *
     * @param message    the doc id
     * @param statusCode the status code
     * @param headers    the headers
     */
    public MigrationServiceException(
            String message, HttpStatusCode statusCode, HttpHeaders headers) {
        super(message + " See exception details: " + statusCode + " " + headers);
    }
}
