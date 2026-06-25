package dev.fixyl.dashboard.file;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class INICompositeFile extends CompositeFile {

    private final String path;
    private final Set<String> keys;

    protected INICompositeFile(String path, Set<String> keys) {
        this.path = path;
        this.keys = Set.copyOf(keys);
    }

    @Override
    protected final Map<String, String> readFile() {
        Map<String, String> values = new HashMap<>();

        try {
            INIConfiguration config = new Configurations().ini(path);

            for (String key : keys) {
                String value = config.getString(key);

                if (value != null) {
                    values.put(key, value);
                }
            }
        } catch (ConfigurationException e) {
            log.debug("Couldn't read INI composite file '{}'", path, e);
            return Map.of();
        }

        return values;
    }

}
