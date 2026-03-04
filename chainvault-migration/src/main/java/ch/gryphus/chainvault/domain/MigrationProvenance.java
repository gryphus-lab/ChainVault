/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.util.Map;
import lombok.Data;

/** The type Migration provenance. */
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MigrationProvenance {
    private String migrationTimestamp;
    private String toolVersion;
    private String operator;
    private Map<String, String> pageHashes;
}
