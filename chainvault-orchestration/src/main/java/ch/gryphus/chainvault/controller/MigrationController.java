/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import ch.gryphus.chainvault.workflow.service.AuditEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final AuditEventService auditEventService;
    private final ObjectMapper objectMapper;

    public MigrationController(AuditEventService auditEventService, ObjectMapper objectMapper) {
        this.auditEventService = auditEventService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> getMigrations(@RequestParam(defaultValue = "100") int limit) {
        return new ResponseEntity<>(
                objectMapper.writeValueAsString(auditEventService.getMigrations(limit)),
                HttpStatus.OK);
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getStats() {
        return new ResponseEntity<>(
                objectMapper.writeValueAsString(auditEventService.getStats()), HttpStatus.OK);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<String> getDetail(@PathVariable String id) {
        return new ResponseEntity<>(
                objectMapper.writeValueAsString(auditEventService.getDetail(id)), HttpStatus.OK);
    }
}
