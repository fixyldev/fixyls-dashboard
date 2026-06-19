package dev.fixyl.dashboard.dto.cpu;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Core {

    private int id;

    private List<CPU> cpus;

}
