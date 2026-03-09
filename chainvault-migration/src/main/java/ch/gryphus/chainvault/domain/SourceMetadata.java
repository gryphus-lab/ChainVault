/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import java.util.List;
import lombok.Data;

/**
 * The type Source metadata.
 */
@Data
public class SourceMetadata {
    private String docId;
    private String title;
    private String creationDate;
    private String clientId;
    private String accountNo;
    private String documentType;
    private String department;
    private String status;
    private int originalSizeBytes;
    private int pageCount;
    private List<String> tags;
    private String payloadUrl;
}
