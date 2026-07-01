package dev.fixyl.dashboard.file;

import static dev.fixyl.dashboard.constant.Paths.RUN_SHUTDOWN_SCHEDULED;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ShutdownFile extends INICompositeFile {

    public ShutdownFile(PathResolver pathResolver) {
        super(
            pathResolver.resolve(RUN_SHUTDOWN_SCHEDULED),
            Set.of("USEC", "MODE", "WALL_MESSAGE")
        );
    }

    public Optional<String> getShutdownTime() {
        return getValue("USEC");
    }

    public Optional<String> getMode() {
        return getValue("MODE");
    }

    public Optional<String> getMessage() {
        return getValue("WALL_MESSAGE");
    }

}
