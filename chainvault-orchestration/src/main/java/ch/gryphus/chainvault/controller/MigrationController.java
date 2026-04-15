/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import ch.gryphus.chainvault.service.SseEmitterService;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

/**
 * The type Migration controller.
 */
@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final AuditEventService auditEventService;
    private final ObjectMapper objectMapper;
    private final SseEmitterService sseEmitterService;

    /**
     * Instantiates a new Migration controller.
     *
     * @param auditEventService the audit event service
     * @param objectMapper      the object mapper
     * @param sseEmitterService the sse emitter service
     */
    public MigrationController(
            AuditEventService auditEventService,
            ObjectMapper objectMapper,
            SseEmitterService sseEmitterService) {
        this.auditEventService = auditEventService;
        this.objectMapper = objectMapper;
        this.sseEmitterService = sseEmitterService;
    }

    /**
     * Retrieve a paginated, optionally sorted list of migrations.
     *
     * If `limit` is less than or equal to 0 or `offset` is negative, the method responds
     * with HTTP 400 and a JSON error message.
     *
     * @param limit   page size; default 100; must be greater than 0
     * @param offset  zero-based record offset; default 0; must be greater than or equal to 0
     * @param sortKey optional field to sort by (default "createdAt")
     * @param sortDir optional sort direction, either "asc" or "desc" (default "desc")
     * @return a ResponseEntity containing a JSON string with the matching migrations and the total count,
     *         or a JSON error message when input validation fails
     */
    @GetMapping
    public ResponseEntity<String> getMigrations(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String sortKey,
            @RequestParam(required = false) String sortDir) {
        if (limit <= 0) {
            return new ResponseEntity<>(
                    objectMapper.writeValueAsString(
                            Map.of("error", "limit must be greater than 0")),
                    HttpStatus.BAD_REQUEST);
        }
        if (offset < 0) {
            return new ResponseEntity<>(
                    objectMapper.writeValueAsString(
                            Map.of("error", "offset must be greater than or equal to 0")),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(
                objectMapper.writeValueAsString(
                        auditEventService.getMigrations(limit, offset, sortKey, sortDir)),
                HttpStatus.OK);
    }

    /**
     * Gets stats.
     *
     * @return the stats
     */
    @GetMapping("/stats")
    public ResponseEntity<String> getStats() {
        return new ResponseEntity<>(
                objectMapper.writeValueAsString(auditEventService.getStats()), HttpStatus.OK);
    }

    /**
     * Retrieve detailed information for a migration by its identifier.
     *
     * @param id the migration identifier
     * @return ResponseEntity containing a JSON string with the migration detail and HTTP 200 (OK) status
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<String> getDetail(@PathVariable String id) {
        return new ResponseEntity<>(
                objectMapper.writeValueAsString(auditEventService.getDetail(id)), HttpStatus.OK);
    }

    /**
     * Subscribe to real-time migration events via Server-Sent Events.
     *
     * @return an SseEmitter that pushes migration events to the client
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToMigrationEvents() {
        String clientId = UUID.randomUUID().toString();
        return sseEmitterService.createEmitter(clientId);
    }
}