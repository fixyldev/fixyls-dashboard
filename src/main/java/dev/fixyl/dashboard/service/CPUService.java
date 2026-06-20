package dev.fixyl.dashboard.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import dev.fixyl.dashboard.dto.cpu.CPU;
import dev.fixyl.dashboard.dto.cpu.Cache;
import dev.fixyl.dashboard.dto.cpu.Cluster;
import dev.fixyl.dashboard.dto.cpu.Core;
import dev.fixyl.dashboard.dto.cpu.Die;
import dev.fixyl.dashboard.dto.cpu.Package;

@Service
public class CPUService implements MetricService<Void, Void> {

    private static final int AVERAGE_CPU_COUNT = 16;

    private static final String CACHES_DIR = "/sys/devices/system/cpu/cpu%s/cache";

    private static final String CORE_ID = "/sys/devices/system/cpu/cpu%s/topology/core_id";
    private static final String CLUSTER_ID = "/sys/devices/system/cpu/cpu%s/topology/cluster_id";
    private static final String DIE_ID = "/sys/devices/system/cpu/cpu%s/topology/die_id";
    private static final String PACKAGE_ID = "/sys/devices/system/cpu/cpu%s/topology/physical_package_id";

    private static final String CORE_CPUS = "/sys/devices/system/cpu/cpu%s/topology/core_cpus_list";
    private static final String CLUSTER_CPUS = "/sys/devices/system/cpu/cpu%s/topology/cluster_cpus_list";
    private static final String DIE_CPUS = "/sys/devices/system/cpu/cpu%s/topology/die_cpus_list";
    private static final String PACKAGE_CPUS = "/sys/devices/system/cpu/cpu%s/topology/package_cpus_list";

    private static final Path PRESENT_CPUS = Path.of("/sys/devices/system/cpu/present");

    @Override
    public Void getStatic() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStatic'");
    }

    @Override
    public Void getUpdate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUpdate'");
    }

    private CPU buildCPU(int cpuId) throws IOException {
        return new CPU(
            cpuId,
            getFilteredCaches(List.of(cpuId), List.of())
        );
    }

    private Core buildCore(List<Integer> cpuIds) throws IOException {
        try {
            List<List<Integer>> alreadyCheckedCpuIds = new ArrayList<>();

            return new Core(
                getCoreId(cpuIds.getFirst()),
                cpuIds.stream().map(cpuId -> {
                    try {
                        alreadyCheckedCpuIds.add(List.of(cpuId));
                        return buildCPU(cpuId);
                    } catch (IOException e) {
                        throw new IOExceptionWrapper(e);
                    }
                }).toList(),
                getFilteredCaches(cpuIds, alreadyCheckedCpuIds)
            );
        } catch (IOExceptionWrapper e) {
            throw e.getIOException();
        }
    }

    private Cluster buildCluster(List<Integer> cpuIds) throws IOException {
        List<Core> cores = new LinkedList<>();
        List<List<Integer>> alreadyCheckedCpuIds = new ArrayList<>();

        int index = 0;
        while (index < cpuIds.size()) {
            int cpuId = cpuIds.get(index);

            List<Integer> coreCPUs = getCoreCPUs(cpuId);
            cores.add(buildCore(coreCPUs));
            alreadyCheckedCpuIds.add(coreCPUs);

            index += coreCPUs.size();
        }

        return new Cluster(
            getClusterId(cpuIds.getFirst()),
            cores,
            getFilteredCaches(cpuIds, alreadyCheckedCpuIds)
        );
    }

    private Die buildDie(List<Integer> cpuIds) throws IOException {
        List<Cluster> clusters = new LinkedList<>();
        List<List<Integer>> alreadyCheckedCpuIds = new ArrayList<>();

        int index = 0;
        while (index < cpuIds.size()) {
            int cpuId = cpuIds.get(index);

            List<Integer> clusterCPUs = getClusterCPUs(cpuId);
            clusters.add(buildCluster(clusterCPUs));
            alreadyCheckedCpuIds.add(clusterCPUs);

            index += clusterCPUs.size();
        }

        return new Die(
            getDieId(cpuIds.getFirst()),
            clusters,
            getFilteredCaches(cpuIds, alreadyCheckedCpuIds)
        );
    }

    private Package buildPackage(List<Integer> cpuIds) throws IOException {
        List<Die> dies = new LinkedList<>();
        List<List<Integer>> alreadyCheckedCpuIds = new ArrayList<>();

        int index = 0;
        while (index < cpuIds.size()) {
            int cpuId = cpuIds.get(index);

            List<Integer> dieCPUs = getDieCPUs(cpuId);
            dies.add(buildDie(dieCPUs));
            alreadyCheckedCpuIds.add(dieCPUs);

            index += dieCPUs.size();
        }

        return new Package(
            getPackageId(cpuIds.getFirst()),
            dies,
            getFilteredCaches(cpuIds, alreadyCheckedCpuIds)
        );
    }

    private List<Package> buildTopology(List<Integer> cpuIds) throws IOException {
        List<Package> packages = new LinkedList<>();

        int index = 0;
        while (index < cpuIds.size()) {
            int cpuId = cpuIds.get(index);

            List<Integer> packageCPUs = getPackageCPUs(cpuId);
            packages.add(buildPackage(packageCPUs));

            index += packageCPUs.size();
        }

        return packages;
    }

    private Set<Cache> getFilteredCaches(List<Integer> cpuIds, List<List<Integer>> alreadyCheckedCpuIds) throws IOException {
        Set<Cache> caches = new HashSet<>();

        for (int cpuId : cpuIds) {
            for (Cache cache : getCaches(cpuId)) {
                if (cpuIds.containsAll(cache.getCpuIds())) {
                    boolean skip = false;

                    for (List<Integer> alreadyCheckedCpuIdList : alreadyCheckedCpuIds) {
                        if (alreadyCheckedCpuIdList.containsAll(cache.getCpuIds())) {
                            skip = true;
                        }
                    }

                    if (skip) {
                        continue;
                    }

                    caches.add(cache);
                }
            }
        }

        return caches;
    }

    private List<Cache> getCaches(int cpuId) throws IOException {
        try (
            Stream<Path> paths = Files.list(Path.of(String.format(CACHES_DIR, cpuId)));
        ) {
            return paths.filter(Files::isDirectory)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(path -> path.startsWith("index"))
                .map(index -> {
                    try {
                        return getCache(cpuId, index);
                    } catch (IOException e) {
                        // Wrap this IOException because we cannot
                        // satisfy the checked exception in a lambda
                        throw new IOExceptionWrapper(e);
                    }
                })
                .toList();
        } catch (IOExceptionWrapper e) {
            // Re-throw the exact same IOException
            // that was previously wrapped
            throw e.getIOException();
        }
    }

    private Cache getCache(int cpuId, String index) throws IOException {
        String cachesDir = String.format(CACHES_DIR, cpuId);

        return new Cache(
            Integer.parseInt(readFile(Path.of(cachesDir, index, "id"))),
            Integer.parseInt(readFile(Path.of(cachesDir, index, "level"))),
            readFile(Path.of(cachesDir, index, "type")),
            parseSize(readFile(Path.of(cachesDir, index, "size"))),
            parseCPUList(readFile(Path.of(cachesDir, index, "shared_cpu_list")))
        );
    }

    private int getCoreId(int cpuId) throws IOException {
        return Integer.parseInt(readFile(String.format(CORE_ID, cpuId)));
    }

    private int getClusterId(int cpuId) throws IOException {
        return Integer.parseInt(readFile(String.format(CLUSTER_ID, cpuId)));
    }

    private int getDieId(int cpuId) throws IOException {
        return Integer.parseInt(readFile(String.format(DIE_ID, cpuId)));
    }

    private int getPackageId(int cpuId) throws IOException {
        return Integer.parseInt(readFile(String.format(PACKAGE_ID, cpuId)));
    }

    private List<Integer> getCoreCPUs(int cpuId) throws IOException {
        return parseCPUList(readFile(String.format(CORE_CPUS, cpuId)));
    }

    private List<Integer> getClusterCPUs(int cpuId) throws IOException {
        return parseCPUList(readFile(String.format(CLUSTER_CPUS, cpuId)));
    }

    private List<Integer> getDieCPUs(int cpuId) throws IOException {
        return parseCPUList(readFile(String.format(DIE_CPUS, cpuId)));
    }

    private List<Integer> getPackageCPUs(int cpuId) throws IOException {
        return parseCPUList(readFile(String.format(PACKAGE_CPUS, cpuId)));
    }

    private List<Integer> getPresentCPUs() throws IOException {
        return parseCPUList(readFile(PRESENT_CPUS));
    }

    private List<Integer> parseCPUList(String cpuList) {
        if (cpuList.isBlank()) {
            return List.of();
        }

        List<Integer> cpuIds = new ArrayList<>(AVERAGE_CPU_COUNT);
        String[] cpuSections = cpuList.split(",");

        for (String cpuSection : cpuSections) {
            String[] cpuRange = cpuSection.split("-");

            if (cpuRange.length == 1) {
                cpuIds.add(Integer.valueOf(cpuRange[0]));
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

    private long parseSize(String size) {
        return Long.parseLong(size.substring(0, size.length() - 1)) * 1024L;
    }

    private String readFile(String path) throws IOException {
        return readFile(Path.of(path));
    }

    private String readFile(Path path) throws IOException {
        return Files.readString(path).trim();
    }

    private static class IOExceptionWrapper extends RuntimeException {

        private final IOException exception;

        public IOExceptionWrapper(IOException exception) {
            super();

            this.exception = exception;
        }

        public IOException getIOException() {
            return this.exception;
        }

    }

}
