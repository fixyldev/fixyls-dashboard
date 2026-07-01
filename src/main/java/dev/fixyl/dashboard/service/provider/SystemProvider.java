package dev.fixyl.dashboard.service.provider;

import static dev.fixyl.dashboard.constant.Paths.PROC_KERNEL_HOSTNAME;
import static dev.fixyl.dashboard.constant.Paths.PROC_KERNEL_RELEASE;

import java.nio.file.Path;
import java.util.Optional;

import org.springframework.stereotype.Component;

import dev.fixyl.dashboard.file.OSReleaseFile;
import dev.fixyl.dashboard.file.PathResolver;
import dev.fixyl.dashboard.file.StatFile;
import dev.fixyl.dashboard.util.FileUtils;
import dev.fixyl.dashboard.util.ParseUtils;

@Component
public class SystemProvider {

    private final OSReleaseFile osReleaseFile;
    private final StatFile statFile;

    private final Path kernelVersionFile;
    private final Path hostnameFile;

    public SystemProvider(OSReleaseFile osReleaseFile, StatFile statFile, PathResolver pathResolver) {
        this.osReleaseFile = osReleaseFile;
        this.statFile = statFile;

        this.kernelVersionFile = pathResolver.resolve(PROC_KERNEL_RELEASE);
        this.hostnameFile = pathResolver.resolve(PROC_KERNEL_HOSTNAME);
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

}
