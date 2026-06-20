package dev.fixyl.dashboard.dto.cpu;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cluster {

    private int id;

    private List<Core> cores;

    private Set<Cache> caches;

}
