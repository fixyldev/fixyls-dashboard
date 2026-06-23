package dev.fixyl.dashboard.service.provider;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Component;

import dev.fixyl.dashboard.util.FileUtils;

@Component
public class FrequencyProvider {

    private static final long MULTIPLIER = 1000L;

    private static final String BASE_FREQ = "/sys/devices/system/cpu/cpu%s/cpufreq/base_frequency";
    private static final String MAX_FREQ = "/sys/devices/system/cpu/cpu%s/cpufreq/scaling_max_freq";
    private static final String MIN_FREQ = "/sys/devices/system/cpu/cpu%s/cpufreq/scaling_min_freq";
    private static final String CURRENT_FREQ = "/sys/devices/system/cpu/cpu%s/cpufreq/scaling_cur_freq";

    public Optional<Long> getBaseFrequency(int cpuId) {
        return readFrequency(BASE_FREQ, cpuId);
    }

    public Optional<Long> getMaxFrequency(int cpuId) {
        return readFrequency(MAX_FREQ, cpuId);
    }

    public Optional<Long> getMinFrequency(int cpuId) {
        return readFrequency(MIN_FREQ, cpuId);
    }

    public Optional<Long> getCurrentFrequency(int cpuId) {
        return readFrequency(CURRENT_FREQ, cpuId);
    }

    private static Optional<Long> readFrequency(String path, Object... args) {
        try {
            return Optional.of(Long.parseLong(FileUtils.readFile(path, args)) * MULTIPLIER);
        } catch (IOException | NumberFormatException _) {
            return Optional.empty();
        }
    }

}
