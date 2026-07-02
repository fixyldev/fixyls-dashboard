package dev.fixyl.dashboard.service.provider;

import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_FREQ_BASE_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_FREQ_CURRENT_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_FREQ_MAX_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_FREQ_MIN_TEMPLATE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.OptionalLong;

import org.springframework.stereotype.Component;

import dev.fixyl.dashboard.file.PathResolver;
import dev.fixyl.dashboard.util.FileUtils;

@Component
public class FrequencyProvider {

    private static final long MULTIPLIER = 1000L;

    private final PathResolver pathResolver;

    public FrequencyProvider(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public OptionalLong getBaseFrequency(int cpuId) {
        return readFrequency(SYS_CPU_FREQ_BASE_TEMPLATE, cpuId);
    }

    public OptionalLong getMaxFrequency(int cpuId) {
        return readFrequency(SYS_CPU_FREQ_MAX_TEMPLATE, cpuId);
    }

    public OptionalLong getMinFrequency(int cpuId) {
        return readFrequency(SYS_CPU_FREQ_MIN_TEMPLATE, cpuId);
    }

    public OptionalLong getCurrentFrequency(int cpuId) {
        return readFrequency(SYS_CPU_FREQ_CURRENT_TEMPLATE, cpuId);
    }

    private OptionalLong readFrequency(String templatePath, Object... args) {
        Path path = pathResolver.resolve(templatePath, args);

        try {
            return OptionalLong.of(Long.parseLong(FileUtils.readFile(path)) * MULTIPLIER);
        } catch (IOException | NumberFormatException _) {
            return OptionalLong.empty();
        }
    }

}
