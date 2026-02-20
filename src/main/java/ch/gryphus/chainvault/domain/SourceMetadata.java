package ch.gryphus.chainvault.domain;

import lombok.Data;

@Data
public class SourceMetadata {
    private String docId;
    private String title;
    private String creationDate;
    private String clientId;
    private String accountNo;
    private String documentType;
    private String hash;
    private String payloadUrl;
}