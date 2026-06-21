package dev.fixyl.dashboard.data.cpu;

import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

public record CPU(
    int id,
    @Nullable String modelName,
    Set<Cache> caches
) {

    void gatherCaches(List<Cache> accumulator) {
        accumulator.addAll(this.caches);
    }

}
