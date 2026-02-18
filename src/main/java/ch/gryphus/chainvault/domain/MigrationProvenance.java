package ch.gryphus.chainvault.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Data;

import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MigrationProvenance {
    private String migrationTimestamp;
    private String toolVersion;
    private String operator;
    private Map<String, String> pageHashes; // filename → SHA-256
}
