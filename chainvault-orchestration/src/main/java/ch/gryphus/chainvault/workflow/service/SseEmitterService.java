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

@Transactional
@Service
public class SseEmitterService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(0L); // 0 = no timeout
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError(ex -> emitters.remove(clientId));

        return emitter;
    }

    public void sendEvent(MigrationEventDto event) {
        emitters.values()
                .forEach(
                        emitter -> {
                            try {
                                emitter.send(
                                        SseEmitter.event().name("migration-event").data(event));
                            } catch (IOException e) {
                                emitters.remove(emitter); // clean up dead emitters
                            }
                        });
    }

    public void sendEventToClient(String clientId, MigrationEventDto event) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("migration-event").data(event));
            } catch (IOException e) {
                emitters.remove(clientId);
            }
        }
    }
}
