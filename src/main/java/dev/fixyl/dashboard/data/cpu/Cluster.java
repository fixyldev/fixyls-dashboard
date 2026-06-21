package dev.fixyl.dashboard.data.cpu;

import java.util.List;
import java.util.Set;

public record Cluster(
    int id,
    List<Core> cores,
    Set<Cache> caches
) {

    void gatherCPUs(List<CPU> accumulator) {
        for (Core core : this.cores) {
            core.gatherCPUs(accumulator);
        }
    }

    void gatherCaches(List<Cache> accumulator) {
        accumulator.addAll(this.caches);

        for (Core core : this.cores) {
            core.gatherCaches(accumulator);
        }
    }

    int countCores() {
        return this.cores.size();
    }

}
