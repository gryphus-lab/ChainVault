/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.model.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigrationEventDto {

    private String id;
    private String migrationId;
    private String eventType;
    private String stepName;
    private String message;
    private Instant timestamp;
    private String status;
    private Long durationMs;
    private String errorCode;
    private String errorMessage;
    private String traceId;
}
