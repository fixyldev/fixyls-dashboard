package dev.fixyl.dashboard.service.provider;

import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_ONLINE;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import dev.fixyl.dashboard.file.CPUInfoFile;
import dev.fixyl.dashboard.file.PathResolver;
import dev.fixyl.dashboard.util.FileUtils;

@Component
public class ProcessorProvider {

    private final CPUInfoFile cpuInfoFile;

    private final Path onlineCPUsFile;

    public ProcessorProvider(CPUInfoFile cpuInfoFile, PathResolver pathResolver) {
        this.cpuInfoFile = cpuInfoFile;
        this.onlineCPUsFile = pathResolver.resolve(SYS_CPU_ONLINE);
    }

    public Optional<String> getModelName() {
        List<String> modelNames = cpuInfoFile.getModelNames();

        if (modelNames.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(cpuInfoFile.getModelNames()
            .stream()
            .filter(StringUtils::hasText)
            .distinct()
            .collect(Collectors.joining(", ")));
    }

    public List<Integer> getCPUs() {
        return FileUtils.readFileOrEmpty(onlineCPUsFile)
            .map(this::parseCPUList)
            .orElse(List.of());
    }

    private List<Integer> parseCPUList(String cpuList) {
        if (cpuList.isBlank()) {
            return List.of();
        }

        List<Integer> cpuIds = new ArrayList<>();
        String[] cpuSections = cpuList.split(",");

        for (String cpuSection : cpuSections) {
            String[] cpuRange = cpuSection.split("-");

            if (cpuRange.length == 1) {
                cpuIds.add(Integer.parseInt(cpuRange[0]));
                continue;
            }

            int min = Integer.parseInt(cpuRange[0]);
            int max = Integer.parseInt(cpuRange[1]);

            for (int index = min; index <= max; index++) {
                cpuIds.add(index);
            }
        }

        return cpuIds;
    }

}
