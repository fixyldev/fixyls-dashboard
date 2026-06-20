package dev.fixyl.dashboard.dto.cpu;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cache {

    private int id;

    private int level;

    private String type;

    private long size;

    private List<Integer> cpuIds;

}
