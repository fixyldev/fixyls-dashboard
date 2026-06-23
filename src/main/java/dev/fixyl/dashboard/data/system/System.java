package dev.fixyl.dashboard.data.system;

import org.jspecify.annotations.Nullable;

public record System(
    @Nullable String os,
    @Nullable String kernel,
    @Nullable Long bootTime
) {}
