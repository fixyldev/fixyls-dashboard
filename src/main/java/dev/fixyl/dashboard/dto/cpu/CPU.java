package dev.fixyl.dashboard.dto.cpu;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CPU {

    private int id;

    private Set<Cache> caches;

}
