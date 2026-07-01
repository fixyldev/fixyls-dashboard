package dev.fixyl.dashboard.data.system;

import org.jspecify.annotations.Nullable;

public record SystemDTO(
    @Nullable String os,
    @Nullable String kernel,
    @Nullable String hostname,
    @Nullable Long bootTime,
    @Nullable ShutdownDTO shutdown
) {}
