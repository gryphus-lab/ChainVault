/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * The type Document.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Document {
    @JacksonXmlProperty(localName = "Identifier")
    private String identifier;

    @JacksonXmlProperty(namespace = "http://purl.org/dc/elements/1.1/")
    private String title;

    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private String created;

    // BusinessContext, Integrity, MigrationProvenance, etc.
}
