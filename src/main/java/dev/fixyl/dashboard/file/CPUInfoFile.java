package dev.fixyl.dashboard.file;

import static dev.fixyl.dashboard.constant.Paths.PROC_CPUINFO;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class CPUInfoFile extends CompositeFile<List<String>> {

    private final Set<String> keys = Set.of("model name");

    private final Path cpuInfoFilePath;

    public CPUInfoFile(PathResolver pathResolver) {
        this.cpuInfoFilePath = pathResolver.resolve(PROC_CPUINFO);
    }

    public List<String> getModelNames() {
        return getValue("model name").orElse(List.of());
    }

    @Override
    protected Map<String, List<String>> readFile() {
        Map<String, List<String>> values = new HashMap<>();

        try (
            BufferedReader reader = Files.newBufferedReader(cpuInfoFilePath);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLines = line.split(":", 2);
                String key = splitLines[0].strip();

                if (splitLines.length != 2 || !keys.contains(key)) {
                    continue;
                }

                String value = splitLines[1].strip();
                values.computeIfAbsent(key, _ -> new ArrayList<>()).add(value);
            }
        } catch (IOException _) {
            return Map.of();
        }

        return values;
    }

}
