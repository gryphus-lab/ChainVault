/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import ch.gryphus.chainvault.service.OrchestrationService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Orchestration rest controller.
 */
@RestController
@RequestMapping("/chainvault")
public class OrchestrationRestController {
    private final OrchestrationService orchestrationService;

    /**
     * Instantiates a new Orchestration rest controller.
     *
     * @param orchestrationService the orchestration service
     */
    @Autowired
    public OrchestrationRestController(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    /**
     * Start process instance response entity.
     *
     * @param payload the payload
     * @return the response entity
     */
    @PostMapping(
            value = "/process",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> startProcessInstance(@RequestBody Map<String, Object> payload) {
        String processId = orchestrationService.startProcess(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        "Process instance started for payload:%s with id:%s"
                                .formatted(payload, processId));
    }
}
