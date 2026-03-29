/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import ch.gryphus.chainvault.model.entity.Migration;
import ch.gryphus.chainvault.model.entity.MigrationDetail;
import ch.gryphus.chainvault.model.entity.MigrationStats;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final AuditEventService auditEventService;

    public MigrationController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping
    public List<Migration> getMigrations(@RequestParam(defaultValue = "100") int limit) {
        return auditEventService.getMigrations(limit);
    }

    @GetMapping("/stats")
    public MigrationStats getStats() {
        return auditEventService.getStats();
    }

    @GetMapping("/{id}/detail")
    public MigrationDetail getDetail(@PathVariable String id) {
        return auditEventService.getDetail(id);
    }
}
