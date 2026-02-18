package ch.gryphus.chainvault.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Document {
    @JacksonXmlProperty(localName = "Identifier")
    private String identifier;

    @JacksonXmlProperty(namespace = "http://purl.org/dc/elements/1.1/")
    private String title;

    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private String created;

    // BusinessContext, Integrity, MigrationProvenance, etc.
    // Add @JacksonXmlRootElement, @JacksonXmlProperty for custom namespaces
}
