package dev.fixyl.dashboard.service.provider;

import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_CACHE_DIR_TEMPLATE;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import dev.fixyl.dashboard.file.PathResolver;
import dev.fixyl.dashboard.util.FileUtils;

@Component
public class CacheProvider {

    private static final long MULTIPLIER = 1024L;

    private final PathResolver pathResolver;

    public CacheProvider(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    // We explicitly filter for non-null elements in the
    // stream. Even if we didn't, items inside a directory
    // should always have a filename either way.
    @SuppressWarnings("null")
    public Set<Cache> getCache(int cpuId) {
        Path cacheDir = pathResolver.resolve(SYS_CPU_CACHE_DIR_TEMPLATE, cpuId);

        try (
            Stream<Path> paths = Files.list(cacheDir);
        ) {
            return paths.filter(Files::isDirectory)
                .map(Path::getFileName)
                .filter(Objects::nonNull)
                .map(Path::toString)
                .filter(path -> path.startsWith("index"))
                .map(index -> readCache(cacheDir, index))
                .collect(Collectors.toSet());
        } catch (IOException | UncheckedIOException | NumberFormatException _) {
            return Set.of();
        }
    }

    private Cache readCache(Path cacheDir, String index) {
        try {
            return new Cache(
                Integer.parseInt(FileUtils.readFile(cacheDir.resolve(index, "id"))),
                Integer.parseInt(FileUtils.readFile(cacheDir.resolve(index, "level"))),
                FileUtils.readFile(cacheDir.resolve(index, "type")),
                parseSize(FileUtils.readFile(cacheDir.resolve(index, "size"))),
                FileUtils.readFile(cacheDir.resolve(index, "shared_cpu_map"))
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private long parseSize(String size) {
        return Long.parseLong(size.substring(0, size.length() - 1)) * MULTIPLIER;
    }

    public static record Cache(
        int id,
        int level,
        String type,
        long size,
        String cpuMap
    ) {}

}
