package dev.fixyl.dashboard.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FileUtils {

    private FileUtils() {}

    public static String readFile(Path path) throws IOException {
        return Files.readString(path).trim();
    }

    public static Optional<String> readFileOrEmpty(Path path) {
        try {
            return Optional.of(Files.readString(path).trim());
        } catch (IOException _) {
            return Optional.empty();
        }
    }

}
