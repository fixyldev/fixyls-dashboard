package dev.fixyl.dashboard.data.cpu;

import java.util.List;

public record Cache(
    int id,
    int level,
    String type,
    long size,
    List<Integer> cpuIds
) {}
