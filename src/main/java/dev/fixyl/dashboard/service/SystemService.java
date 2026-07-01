package dev.fixyl.dashboard.service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dev.fixyl.dashboard.dto.system.ShutdownDTO;
import dev.fixyl.dashboard.dto.system.SystemDTO;
import dev.fixyl.dashboard.service.provider.SystemProvider;
import dev.fixyl.dashboard.sse.SseEmitterRegistry;

@Service
public class SystemService {

    private static final long INTERVAL_MILLIS = 1000L;
    private static final String EVENT_NAME = "system";

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final SseEmitterRegistry registry;
    private final SystemProvider systemProvider;

    public SystemService(SseEmitterRegistry registry, SystemProvider systemProvider) {
        this.registry = registry;
        this.systemProvider = systemProvider;
    }

    @Scheduled(fixedRate = INTERVAL_MILLIS)
    private void tick() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }

        try {
            update();
        } finally {
            isRunning.lazySet(false);
        }
    }

    private void update() {
        registry.broadcast(EVENT_NAME, buildSystemDTO());
    }

    private SystemDTO buildSystemDTO() {
        return new SystemDTO(
            systemProvider.getOSName().orElse(null),
            systemProvider.getKernelVersion().orElse(null),
            systemProvider.getHostname().orElse(null),
            systemProvider.getBootTime().orElse(null),
            buildShutdownDTO()
        );
    }

    private @Nullable ShutdownDTO buildShutdownDTO() {
        Optional<String> mode = systemProvider.getShutdownMode();

        if (mode.isEmpty()) {
            return null;
        }

        return new ShutdownDTO(
            systemProvider.getShutdownTime().orElse(null),
            mode.orElseThrow(),
            systemProvider.getShutdownMessage().orElse(null)
        );
    }

}
