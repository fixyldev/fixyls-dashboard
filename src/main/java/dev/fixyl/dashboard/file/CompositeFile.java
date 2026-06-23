package dev.fixyl.dashboard.file;

import java.util.Map;
import java.util.Optional;

public abstract class CompositeFile {

    private static final long TTL_MILLIS = 900L;

    private Map<String, String> values;
    private long expiry = -1L;

    protected abstract Map<String, String> readFile();

    protected final synchronized Optional<String> getValue(String key) {
        long now = System.currentTimeMillis();

        if (now >= expiry) {
            values = Map.copyOf(readFile());
            expiry = now + TTL_MILLIS;
        }

        return Optional.ofNullable(values.get(key));
    }

}
