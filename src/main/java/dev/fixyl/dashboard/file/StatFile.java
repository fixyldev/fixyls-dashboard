package dev.fixyl.dashboard.file;

import static dev.fixyl.dashboard.constant.Paths.PROC_STAT;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class StatFile extends CompositeFile {

    private static final Set<String> keys = Set.of("btime");

    private final Path statFilePath;

    public StatFile(PathResolver pathResolver) {
        this.statFilePath = pathResolver.resolve(PROC_STAT);
    }

    public Optional<String> getBootTime() {
        return getValue("btime");
    }

    @Override
    protected Map<String, String> readFile() {
        Map<String, String> values = new HashMap<>();

        try (
            BufferedReader reader = Files.newBufferedReader(statFilePath);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLines = line.split(" ", 2);
                String key = splitLines[0].strip();

                if (splitLines.length != 2 || !keys.contains(key)) {
                    continue;
                }

                String value = splitLines[1].strip();
                values.put(key, value);
            }
        } catch (IOException _) {
            return Map.of();
        }

        return values;
    }

}
