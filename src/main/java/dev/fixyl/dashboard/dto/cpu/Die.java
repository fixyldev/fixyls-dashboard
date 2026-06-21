package dev.fixyl.dashboard.dto.cpu;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class Die {

    private int id;

    @NonNull
    private List<Cluster> clusters;

    @NonNull
    private Set<Cache> caches;

}
