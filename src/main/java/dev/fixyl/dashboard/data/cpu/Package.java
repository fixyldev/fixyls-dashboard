package dev.fixyl.dashboard.data.cpu;

import java.util.List;
import java.util.Set;

public record Package(
    int id,
    List<Die> dies,
    Set<Cache> caches
) {

    void gatherCPUs(List<CPU> accumulator) {
        for (Die die : this.dies) {
            die.gatherCPUs(accumulator);
        }
    }

    void gatherCaches(List<Cache> accumulator) {
        accumulator.addAll(this.caches);

        for (Die die : this.dies) {
            die.gatherCaches(accumulator);
        }
    }

    int countCores() {
        return this.dies.stream().mapToInt(Die::countCores).sum();
    }

}
