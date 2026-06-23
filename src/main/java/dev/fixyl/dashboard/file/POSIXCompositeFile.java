package dev.fixyl.dashboard.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class POSIXCompositeFile extends CompositeFile {

    private final String path;
    private final Set<String> keys;

    protected POSIXCompositeFile(String path, Set<String> keys) {
        this.path = path;
        this.keys = Set.copyOf(keys);
    }

    // TODO: Improve this small POSIX variable assignment parser
    //       to include all edge cases, etc.
    @Override
    protected final Map<String, String> readFile() {
        Map<String, String> values = new HashMap<>();

        try (
            FileReader fileReader = new FileReader(path);
            BufferedReader reader = new BufferedReader(fileReader);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLines = line.split("=", 2);
                String key = splitLines[0].strip();

                if (splitLines.length != 2 || !keys.contains(key)) {
                    continue;
                }

                String value = splitLines[1].strip();
                int length = value.length();

                if (
                    length >= 2
                    && value.startsWith("\"")
                    && value.endsWith("\"")
                ) {
                    values.put(key, value.substring(1, length - 1));
                } else {
                    values.put(key, value);
                }
            }
        } catch (IOException _) {
            return Map.of();
        }

        return values;
    }

}
