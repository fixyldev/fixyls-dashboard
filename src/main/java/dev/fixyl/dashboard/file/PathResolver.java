package dev.fixyl.dashboard.file;

import java.nio.file.Path;

public interface PathResolver {

    Path resolve(String path);

    default Path resolve(String path, Object... args) {
        return resolve(path.formatted(args));
    }

}
