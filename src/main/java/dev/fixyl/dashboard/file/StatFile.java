package dev.fixyl.dashboard.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class StatFile extends CompositeFile {

    private static final String STAT_PATH = "/proc/stat";

    private static final Set<String> keys = Set.of("btime");

    public Optional<String> getBootTime() {
        return getValue("btime");
    }

    @Override
    protected Map<String, String> readFile() {
        Map<String, String> values = new HashMap<>();

        try (
            FileReader fileReader = new FileReader(STAT_PATH);
            BufferedReader reader = new BufferedReader(fileReader);
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
