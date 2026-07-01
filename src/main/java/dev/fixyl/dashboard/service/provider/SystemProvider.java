package dev.fixyl.dashboard.service.provider;

import static dev.fixyl.dashboard.constant.Paths.PROC_KERNEL_HOSTNAME;
import static dev.fixyl.dashboard.constant.Paths.PROC_KERNEL_RELEASE;
import static dev.fixyl.dashboard.constant.Paths.RUN_SHUTDOWN_DIR;
import static dev.fixyl.dashboard.constant.Paths.RUN_SHUTDOWN_SCHEDULED;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.stereotype.Component;

import dev.fixyl.dashboard.file.OSReleaseFile;
import dev.fixyl.dashboard.file.PathResolver;
import dev.fixyl.dashboard.file.ShutdownFile;
import dev.fixyl.dashboard.file.StatFile;
import dev.fixyl.dashboard.util.FileUtils;
import dev.fixyl.dashboard.util.ParseUtils;

@Component
public class SystemProvider {

    private final OSReleaseFile osReleaseFile;
    private final StatFile statFile;
    private final ShutdownFile shutdownFile;

    private final Path kernelVersionFile;
    private final Path hostnameFile;
    private final Path shutdownDir;
    private final Path shutdownScheduleFile;

    public SystemProvider(OSReleaseFile osReleaseFile, StatFile statFile, ShutdownFile shutdownFile, PathResolver pathResolver) {
        this.osReleaseFile = osReleaseFile;
        this.statFile = statFile;
        this.shutdownFile = shutdownFile;

        this.kernelVersionFile = pathResolver.resolve(PROC_KERNEL_RELEASE);
        this.hostnameFile = pathResolver.resolve(PROC_KERNEL_HOSTNAME);
        this.shutdownDir = pathResolver.resolve(RUN_SHUTDOWN_DIR);
        this.shutdownScheduleFile = pathResolver.resolve(RUN_SHUTDOWN_SCHEDULED);
    }

    public Optional<String> getKernelVersion() {
        return FileUtils.readFileOrEmpty(kernelVersionFile);
    }

    public Optional<String> getOSName() {
        return osReleaseFile.getPrettyName();
    }

    public Optional<String> getHostname() {
        return FileUtils.readFileOrEmpty(hostnameFile);
    }

    public Optional<Long> getBootTime() {
        return statFile.getBootTime().flatMap(ParseUtils::parseLongOrEmpty);
    }

    public Optional<Long> getShutdownTime() {
        return shutdownFile.getShutdownTime().flatMap(ParseUtils::parseLongOrEmpty);
    }

    public Optional<String> getShutdownMode() {
        // Shutdown directory must exist and is accessible,
        // otherwise we don't have shutdown information
        if (!Files.isDirectory(shutdownDir) || !Files.isExecutable(shutdownDir)) {
            return Optional.empty();
        }

        // If the shutdown file exists but is not readable,
        // we also don't have shutdown information
        if (Files.isRegularFile(shutdownScheduleFile) && !Files.isReadable(shutdownScheduleFile)) {
            return Optional.empty();
        }

        return shutdownFile.getMode().or(() -> Optional.of("not planned"));
    }

    public Optional<String> getShutdownMessage() {
        return shutdownFile.getMessage();
    }

}
