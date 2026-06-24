package dev.fixyl.dashboard.service;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import dev.fixyl.dashboard.data.system.System;
import dev.fixyl.dashboard.data.system.SystemUpdate;
import dev.fixyl.dashboard.service.provider.SystemProvider;

@Service
public class SystemService extends TickingService {

    private final SystemProvider systemProvider;

    private @Nullable System system;
    private @Nullable SystemUpdate update;

    public SystemService(SystemProvider systemProvider) {
        this.systemProvider = systemProvider;
    }

    public Optional<System> getSystem() {
        return Optional.ofNullable(system);
    }

    public Optional<SystemUpdate> getUpdate() {
        return Optional.ofNullable(update);
    }

    public void buildSystem() {
        system = new System(
            systemProvider.getOSName().orElse(null),
            systemProvider.getKernelVersion().orElse(null),
            systemProvider.getHostname().orElse(null),
            systemProvider.getBootTime().orElse(null)
        );
    }

    @Override
    protected void update() {
    }

    @EventListener(ApplicationReadyEvent.class)
    private void init() {
        buildSystem();
    }

}
