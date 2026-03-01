/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * The type Migration context.
 */
@Data
public class MigrationContext {
    private String docId;
    private String metadataHash;
    private String payloadHash;
    private String zipHash;
    private String pdfHash;
    private Map<String, String> pageHashes = new HashMap<>();

    /**
     * Add page hash.
     *
     * @param name     the name
     * @param pageHash the page hash
     */
    public void addPageHash(String name, String pageHash) {
        pageHashes.put(name, pageHash);
    }
}
