package dev.fixyl.dashboard.file;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RootDirectoryPathResolver implements PathResolver {

    @Value("${fixyls-dashboard.path.host-root-dir}")
    private Path rootDir;

    @Override
    public Path resolve(String path) {
        return rootDir.resolve(path);
    }

}
