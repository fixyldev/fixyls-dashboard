package dev.fixyl.dashboard.controller;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private static final long TIMEOUT = 60_000L;  // 60 seconds

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter createEventStream() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        Runnable cleanup = () -> emitters.remove(emitter);

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(_ -> cleanup.run());

        emitters.add(emitter);

        try {
            emitter.send(SseEmitter.event().name("init").data("established"));
        } catch (IOException | IllegalStateException _) {
            cleanup.run();
        }

        return emitter;
    }

    public void sendEvent(String name, Object data) {
        String json = objectMapper.writeValueAsString(data);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(name).data(json));
            } catch (IOException | IllegalStateException _) {
                emitters.remove(emitter);
            }
        }
    }

    public boolean isClientWaiting() {
        return !emitters.isEmpty();
    }
}
