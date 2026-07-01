package dev.fixyl.dashboard.dto.processor;

import org.jspecify.annotations.Nullable;

public record CacheDTO(
    @Nullable Long level1,
    @Nullable Long level2,
    @Nullable Long level3,
    @Nullable Long level4
) {}
