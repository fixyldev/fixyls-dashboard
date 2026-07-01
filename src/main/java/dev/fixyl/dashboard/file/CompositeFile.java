package dev.fixyl.dashboard.file;

import java.util.Map;
import java.util.Optional;

public abstract class CompositeFile<T> {

    private static final long TTL_MILLIS = 900L;

    private Map<String, T> values;
    private long expiry = -1L;

    protected abstract Map<String, T> readFile();

    protected final synchronized Optional<T> getValue(String key) {
        long now = System.currentTimeMillis();

        if (now >= expiry) {
            values = Map.copyOf(readFile());
            expiry = now + TTL_MILLIS;
        }

        return Optional.ofNullable(values.get(key));
    }

}
