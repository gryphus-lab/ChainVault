/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import lombok.Data;

/**
 * The type Archival metadata.
 */
@XmlRootElement(name = "ArchivalMetadata")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ArchivalMetadata {
    private String documentId;
    private String title;
    private String creationDate;
    private String clientId;
    private String documentType;
    private int pageCount;

    private String payloadHash;
    private String zipHash;
    private String pdfHash;

    private MigrationProvenance provenance;
    private Map<String, Object> customFields;
}
