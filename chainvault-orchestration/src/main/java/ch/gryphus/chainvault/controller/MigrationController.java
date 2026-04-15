/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import ch.gryphus.chainvault.workflow.service.AuditEventService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

/**
 * The type Migration controller.
 */
@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final AuditEventService auditEventService;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new Migration controller.
     *
     * @param auditEventService the audit event service
     * @param objectMapper      the object mapper
     */
    public MigrationController(AuditEventService auditEventService, ObjectMapper objectMapper) {
        this.auditEventService = auditEventService;
        this.objectMapper = objectMapper;
    }

    /**
     * Gets migrations.
     *
     * @param limit   the page size (default 100)
     * @param offset  the zero-based record offset (default 0)
     * @param sortKey the field to sort by (default "createdAt")
     * @param sortDir the sort direction: "asc" or "desc" (default "desc")
     * @return a paginated response containing the matching migrations and the total count
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
     * Gets detail.
     *
     * @param id the id
     * @return the detail
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<String> getDetail(@PathVariable String id) {
        return new ResponseEntity<>(
                objectMapper.writeValueAsString(auditEventService.getDetail(id)), HttpStatus.OK);
    }
}