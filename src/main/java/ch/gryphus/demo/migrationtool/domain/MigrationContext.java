package ch.gryphus.demo.migrationtool.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MigrationContext {
    private final String docId;
    private String payloadHash;
    private String zipHash;
    private String pdfHash;
    private Map<String, String> pageHashes = new HashMap<>();

    public void addPageHash(String name, String pageHash) {
        pageHashes.put(name, pageHash);
    }
}
