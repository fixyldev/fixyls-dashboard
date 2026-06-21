package dev.fixyl.dashboard.dto.cpu;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class Cache {

    private int id;

    private int level;

    @NonNull
    private String type;

    private long size;

    @NonNull
    private List<Integer> cpuIds;

}
