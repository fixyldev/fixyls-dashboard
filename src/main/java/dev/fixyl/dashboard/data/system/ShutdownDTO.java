package dev.fixyl.dashboard.data.system;

import org.jspecify.annotations.Nullable;

public record ShutdownDTO(
    @Nullable Long shutdownTime,
    String mode,
    @Nullable String message
) {}
