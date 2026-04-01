/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import ch.gryphus.chainvault.service.SseEmitterService;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * The type Migration sse controller.
 */
@RestController
@RequestMapping("/api/migrations")
public class MigrationSseController {

    private final SseEmitterService sseEmitterService;

    /**
     * Instantiates a new Migration sse controller.
     *
     * @param sseEmitterService the sse emitter service
     */
    public MigrationSseController(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    /**
     * Stream migration events sse emitter.
     *
     * @param clientId the client id
     * @return the sse emitter
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMigrationEvents(@RequestParam(required = false) String clientId) {
        String id = clientId != null ? clientId : UUID.randomUUID().toString();
        return sseEmitterService.createEmitter(id);
    }
}
