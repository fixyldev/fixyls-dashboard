package dev.fixyl.dashboard.service.provider;

import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_CORE_ID_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_PACKAGE_ID_TEMPLATE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.OptionalInt;

import org.springframework.stereotype.Component;

import dev.fixyl.dashboard.file.PathResolver;
import dev.fixyl.dashboard.util.FileUtils;

@Component
public class TopologyProvider {

    private final PathResolver pathResolver;

    public TopologyProvider(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public OptionalInt getPackageId(int cpuId) {
        return readCount(SYS_CPU_TOPO_PACKAGE_ID_TEMPLATE, cpuId);
    }

    public OptionalInt getCoreId(int cpuId) {
        return readCount(SYS_CPU_TOPO_CORE_ID_TEMPLATE, cpuId);
    }

    private OptionalInt readCount(String templatePath, Object... args) {
        Path path = pathResolver.resolve(templatePath, args);

        try {
            return OptionalInt.of(Integer.parseInt(FileUtils.readFile(path)));
        } catch (IOException | NumberFormatException _) {
            return OptionalInt.empty();
        }
    }

}
