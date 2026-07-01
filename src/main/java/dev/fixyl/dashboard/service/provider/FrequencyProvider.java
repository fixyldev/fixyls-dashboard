package dev.fixyl.dashboard.service.provider;

import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_FREQ_BASE_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_FREQ_CURRENT_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_FREQ_MAX_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_FREQ_MIN_TEMPLATE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

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

    public Optional<Long> getBaseFrequency(int cpuId) {
        return readFrequency(SYS_CPU_FREQ_BASE_TEMPLATE, cpuId);
    }

    public Optional<Long> getMaxFrequency(int cpuId) {
        return readFrequency(SYS_CPU_FREQ_MAX_TEMPLATE, cpuId);
    }

    public Optional<Long> getMinFrequency(int cpuId) {
        return readFrequency(SYS_CPU_FREQ_MIN_TEMPLATE, cpuId);
    }

    public Optional<Long> getCurrentFrequency(int cpuId) {
        return readFrequency(SYS_CPU_FREQ_CURRENT_TEMPLATE, cpuId);
    }

    private Optional<Long> readFrequency(String templatePath, Object... args) {
        Path path = pathResolver.resolve(templatePath, args);

        try {
            return Optional.of(Long.parseLong(FileUtils.readFile(path)) * MULTIPLIER);
        } catch (IOException | NumberFormatException _) {
            return Optional.empty();
        }
    }

}
