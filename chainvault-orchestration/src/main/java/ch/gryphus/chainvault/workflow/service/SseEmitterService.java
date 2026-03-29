/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.service;

import ch.gryphus.chainvault.model.dto.MigrationEventDto;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

@Transactional
@Service
public class SseEmitterService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseEmitterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError(ex -> emitters.remove(clientId));

        return emitter;
    }

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
