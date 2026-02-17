package ch.gryphus.demo.migrationtool.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.Map;

// ArchivalMetadata.java
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
