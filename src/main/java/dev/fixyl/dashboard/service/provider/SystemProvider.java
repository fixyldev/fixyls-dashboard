package dev.fixyl.dashboard.service.provider;

import java.nio.file.Path;
import java.util.Optional;

import org.springframework.stereotype.Component;

import dev.fixyl.dashboard.file.OSReleaseFile;
import dev.fixyl.dashboard.file.StatFile;
import dev.fixyl.dashboard.util.FileUtils;
import dev.fixyl.dashboard.util.ParseUtils;

@Component
public class SystemProvider {

    private static final Path KERNEL_RELEASE = Path.of("/proc/sys/kernel/osrelease");

    private final OSReleaseFile osReleaseFile;
    private final StatFile statFile;

    public SystemProvider(OSReleaseFile osReleaseFile, StatFile statFile) {
        this.osReleaseFile = osReleaseFile;
        this.statFile = statFile;
    }

    public Optional<String> getKernelVersion() {
        return FileUtils.readFileOrEmpty(KERNEL_RELEASE);
    }

    public Optional<String> getOSName() {
        return osReleaseFile.getPrettyName();
    }

    public Optional<Long> getBootTime() {
        return statFile.getBootTime().flatMap(ParseUtils::parseLongOrEmpty);
    }

}
