package dev.fixyl.dashboard.file;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

@Component
@Profile("test")
@Primary
public class InMemoryFilesystemPathResolver implements PathResolver {

    private static final String ROOT_DIR = "/";

    private final FileSystem filesystem = Jimfs.newFileSystem(Configuration.unix());

    @Override
    public Path resolve(String path) {
        return filesystem.getPath(ROOT_DIR, path);
    }

}
