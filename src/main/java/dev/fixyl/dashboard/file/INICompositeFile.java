package dev.fixyl.dashboard.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class INICompositeFile extends CompositeFile<String> {

    private final Path path;
    private final Set<String> keys;

    protected INICompositeFile(Path path, Set<String> keys) {
        this.path = path;
        this.keys = Set.copyOf(keys);
    }

    @Override
    protected final Map<String, String> readFile() {
        Map<String, String> values = new HashMap<>();

        try (
            BufferedReader reader = Files.newBufferedReader(path);
        ) {
            INIConfiguration config = new INIConfiguration();
            new FileHandler(config).load(reader);

            for (String key : keys) {
                String value = config.getString(key);

                if (value != null) {
                    values.put(key, value);
                }
            }
        } catch (IOException | ConfigurationException e) {
            log.debug("Couldn't read INI composite file '{}'", path, e);
            return Map.of();
        }

        return values;
    }

}
