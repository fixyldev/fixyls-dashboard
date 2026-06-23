package dev.fixyl.dashboard.data.cpu;

import java.util.Map;

public record ProcessorUpdate(
    Map<Integer, String> currentFrequencies
) {}
