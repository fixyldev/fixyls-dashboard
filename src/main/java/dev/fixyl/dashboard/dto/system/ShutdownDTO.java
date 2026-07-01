package dev.fixyl.dashboard.dto.system;

import org.jspecify.annotations.Nullable;

public record ShutdownDTO(
    @Nullable Long shutdownTime,
    String mode,
    @Nullable String message
) {}
