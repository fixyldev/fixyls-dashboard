package dev.fixyl.dashboard.dto.processor;

import java.util.Map;

import org.jspecify.annotations.Nullable;

public record FrequencyDTO(
    @Nullable Long baseFrequency,
    @Nullable Long maxFrequency,
    @Nullable Long minFrequency,
    Map<Integer, Long> currentFrequencies
) {}
