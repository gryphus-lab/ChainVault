/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.util;

import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.exception.MigrationServiceException;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * The type Source api utils.
 */
public final class SourceApiUtils {
    private SourceApiUtils() {
        /* This utility class should not be instantiated */
    }

    /**
     * Gets source metadata.
     *
     * @param restClient the rest client
     * @param docId      the doc id
     * @return the source metadata
     */
    public static SourceMetadata getSourceMetadata(RestClient restClient, String docId) {
        return restClient
                .get()
                .uri("/documents/{id}", docId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange(
                        (_, response) -> {
                            if (response.getStatusCode().is4xxClientError()) {
                                throw new MigrationServiceException(
                                        "Unable to find document with id: %s".formatted(docId),
                                        response.getStatusCode(),
                                        response.getHeaders());
                            } else {
                                return response.bodyTo(SourceMetadata.class);
                            }
                        });
    }

    /**
     * Get payload bytes byte [ ].
     *
     * @param restClient the rest client
     * @param docId      the doc id
     * @param meta       the meta
     * @return the byte [ ]
     */
    public static byte[] getPayloadBytes(RestClient restClient, String docId, SourceMetadata meta) {
        return restClient
                .get()
                .uri(meta.getPayloadUrl())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange(
                        (_, response) -> {
                            if (response.getStatusCode().is4xxClientError()) {
                                throw new MigrationServiceException(
                                        "Unable to find payload for document with id: %s"
                                                .formatted(docId),
                                        response.getStatusCode(),
                                        response.getHeaders());
                            } else {
                                return response.bodyTo(byte[].class);
                            }
                        });
    }
}
