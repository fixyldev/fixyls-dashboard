package dev.fixyl.dashboard.file;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class OSReleaseFile extends POSIXCompositeFile {

    private static final String OS_RELEASE_PATH = "/etc/os-release";

    public OSReleaseFile() {
        super(OS_RELEASE_PATH, Set.of("PRETTY_NAME"));
    }

    public Optional<String> getPrettyName() {
        return getValue("PRETTY_NAME");
    }

}
