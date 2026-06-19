package dev.fixyl.dashboard.dto.cpu;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cluster {

    private int id;

    private List<Core> cores;

}
