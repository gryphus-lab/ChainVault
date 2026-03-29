/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.model.entity;

import java.time.Instant;
import lombok.Data;

@Data
public class Migration {
    private String id;
    private String docId;
    private String title;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private String processInstanceKey;
    private String traceId;
    private int pageCount;
    private Boolean ocrAttempted;
    private Boolean ocrSuccess;
    private Integer ocrPageCount;
    private Long ocrTotalTextLength;
    private String failureReason;
}
