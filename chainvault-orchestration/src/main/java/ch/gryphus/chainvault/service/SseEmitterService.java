/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.model.dto.MigrationEventDto;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

/**
 * The type Sse emitter service.
 */
@Service
public class SseEmitterService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new Sse emitter service.
     *
     * @param objectMapper the object mapper
     */
    @SuppressWarnings("spotbugs:EI_EXPOSE_REP2")
    public SseEmitterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Create emitter sse emitter.
     *
     * @param clientId the client id
     * @return the sse emitter
     */
    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError(ex -> emitters.remove(clientId));

        return emitter;
    }

    /**
     * Send event.
     *
     * @param event the event
     */
    public void sendEvent(MigrationEventDto event) {
        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (Exception _) {
            json = "{}";
        }

        String finalJson = json;
        emitters.values()
                .removeIf(
                        emitter -> {
                            try {
                                emitter.send(finalJson);
                                return false;
                            } catch (IOException _) {
                                return true; // remove dead emitter
                            }
                        });
    }

    /**
     * Send event to client.
     *
     * @param clientId the client id
     * @param event    the event
     */
    public void sendEventToClient(String clientId, MigrationEventDto event) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("migration-event").data(event));
            } catch (IOException _) {
                emitters.remove(clientId);
            }
        }
    }
}
