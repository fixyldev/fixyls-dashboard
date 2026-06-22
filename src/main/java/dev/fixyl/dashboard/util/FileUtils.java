package dev.fixyl.dashboard.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {

    private FileUtils() {}

    public static String readFile(String path, Object... args) throws IOException {
        return readFile(String.format(path, args));
    }

    public static String readFile(String path) throws IOException {
        return readFile(Path.of(path));
    }

    public static String readFile(Path path) throws IOException {
        return Files.readString(path).trim();
    }

}
