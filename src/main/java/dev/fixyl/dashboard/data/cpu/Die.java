package dev.fixyl.dashboard.data.cpu;

import java.util.List;
import java.util.Set;

public record Die(
    int id,
    List<Cluster> clusters,
    Set<Cache> caches
) {

    void gatherCPUs(List<CPU> accumulator) {
        for (Cluster cluster : this.clusters) {
            cluster.gatherCPUs(accumulator);
        }
    }

    void gatherCaches(List<Cache> accumulator) {
        accumulator.addAll(this.caches);

        for (Cluster cluster : this.clusters) {
            cluster.gatherCaches(accumulator);
        }
    }

    int countCores() {
        return this.clusters.stream().mapToInt(Cluster::countCores).sum();
    }

}
