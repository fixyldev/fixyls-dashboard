package dev.fixyl.dashboard.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Scheduled;

public abstract class TickingService {

    private static final long INTERVAL_MILLIS = 1000L;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    protected abstract void update();

    @Scheduled(fixedRate = INTERVAL_MILLIS)
    private void tick() {
        if (!isRunning.compareAndSet(false, true)) {
            // Skip this interval if previous interval didn't finish in time
            return;
        }

        try {
            update();
        } finally {
            isRunning.set(false);
        }
    }

}
