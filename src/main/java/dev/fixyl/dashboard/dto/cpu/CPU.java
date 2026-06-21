package dev.fixyl.dashboard.dto.cpu;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class CPU {

    private int id;

    private String modelName;

    @NonNull
    private Set<Cache> caches;

}
