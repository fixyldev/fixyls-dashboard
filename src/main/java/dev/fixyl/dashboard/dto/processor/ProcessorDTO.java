package dev.fixyl.dashboard.dto.processor;

import org.jspecify.annotations.Nullable;

public record ProcessorDTO(
    @Nullable String modelName,
    @Nullable Integer socketCount,
    @Nullable Integer coreCount,
    @Nullable Integer threadCount,
    @Nullable CacheDTO cache,
    @Nullable FrequencyDTO frequency
) {}
