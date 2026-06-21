package dev.fixyl.dashboard.data.cpu;

import java.util.List;
import java.util.Set;

public record Core(
    int id,
    List<CPU> cpus,
    Set<Cache> caches
) {

    void gatherCPUs(List<CPU> accumulator) {
        accumulator.addAll(this.cpus);
    }

    void gatherCaches(List<Cache> accumulator) {
        accumulator.addAll(this.caches);

        for (CPU cpu : this.cpus) {
            cpu.gatherCaches(accumulator);
        }
    }

}
