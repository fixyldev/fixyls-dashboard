package dev.fixyl.dashboard.dto.processor;

import java.util.Map;

import org.jspecify.annotations.Nullable;

public record FrequencyDTO(
    @Nullable Long base,
    @Nullable Long max,
    @Nullable Long min,
    Map<Integer, Long> current
) {}
