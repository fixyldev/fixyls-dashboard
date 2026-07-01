package dev.fixyl.dashboard.service;

import static dev.fixyl.dashboard.constant.Paths.PROC_CPUINFO;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_CACHE_DIR_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_OFFLINE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_ONLINE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_PRESENT;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_CLUSTER_CPUS_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_CLUSTER_ID_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_CORE_CPUS_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_CORE_ID_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_DIE_CPUS_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_DIE_ID_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_PACKAGE_CPUS_TEMPLATE;
import static dev.fixyl.dashboard.constant.Paths.SYS_CPU_TOPO_PACKAGE_ID_TEMPLATE;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dev.fixyl.dashboard.controller.SseController;
import dev.fixyl.dashboard.data.cpu.CPU;
import dev.fixyl.dashboard.data.cpu.Cache;
import dev.fixyl.dashboard.data.cpu.Cluster;
import dev.fixyl.dashboard.data.cpu.Core;
import dev.fixyl.dashboard.data.cpu.Die;
import dev.fixyl.dashboard.data.cpu.Package;
import dev.fixyl.dashboard.data.cpu.Processor;
import dev.fixyl.dashboard.data.cpu.ProcessorUpdate;
import dev.fixyl.dashboard.file.PathResolver;
import dev.fixyl.dashboard.service.provider.FrequencyProvider;
import dev.fixyl.dashboard.util.DataUtils;

@Service
public class ProcessorService {

    private static final long UPDATE_INTERVAL = 1000L;  // 1 second

    private static final int AVERAGE_CPU_COUNT = 16;

    private final SseController sseController;
    private final FrequencyProvider frequencyProvider;
    private final PathResolver pathResolver;

    private final Path cpuInfoFile;
    private final Path presentCPUsFile;
    private final Path onlineCPUsFile;
    private final Path offlineCPUsFile;

    private Map<Integer, String> cpuModelNames;

    @Nullable
    private Processor processor;

    public ProcessorService(SseController sseController, FrequencyProvider frequencyProvider, PathResolver pathResolver) {
        this.sseController = sseController;
        this.frequencyProvider = frequencyProvider;
        this.pathResolver = pathResolver;

        this.cpuInfoFile = pathResolver.resolve(PROC_CPUINFO);
        this.presentCPUsFile = pathResolver.resolve(SYS_CPU_PRESENT);
        this.onlineCPUsFile = pathResolver.resolve(SYS_CPU_ONLINE);
        this.offlineCPUsFile = pathResolver.resolve(SYS_CPU_OFFLINE);

        // TODO: Clear this constructor and initialize data differently
        this.cpuModelNames = Map.of();

        this.rebuildProcessor();
    }

    @Scheduled(fixedRate = UPDATE_INTERVAL)
    private void update() {
        // TODO: Use AtomicBoolean to skip an interval if we cannot finish within a second and the next interval would run in parallel
        if (!sseController.isClientWaiting()) {
            return;
        }

        sseController.sendEvent("processorUpdate", getUpdate());
    }

    public boolean rebuildProcessor() {
        this.cpuModelNames = getModelNames();

        try {
            this.processor = new Processor(buildTopology(getOnlineCPUs()));
        } catch (IOException _) {
            return false;
        }

        return true;
    }

    public Optional<Processor> getProcessor() {
        return Optional.ofNullable(this.processor);
    }

    public Optional<ProcessorUpdate> getUpdate() {
        Map<Integer, @Nullable String> freqs = new TreeMap<>();

        try {
            for (Integer cpuId : getOnlineCPUs()) {
                Optional<Long> freq = frequencyProvider.getCurrentFrequency(cpuId);

                if (freq.isEmpty()) {
                    return Optional.empty();
                }

                freqs.put(cpuId, DataUtils.hertzToString(freq.orElseThrow()));
            }

            for (Integer cpuId : getOfflineCPUs()) {
                freqs.put(cpuId, "offline");
            }
        } catch (IOException _) {
            return Optional.empty();
        }

        return Optional.of(new ProcessorUpdate(freqs));
    }

    private CPU buildCPU(int cpuId) {
        return new CPU(
            cpuId,
            this.cpuModelNames.get(cpuId),
            getFilteredCaches(List.of(cpuId), List.of()),
            frequencyProvider.getBaseFrequency(cpuId).orElse(null),
            frequencyProvider.getMaxFrequency(cpuId).orElse(null),
            frequencyProvider.getMinFrequency(cpuId).orElse(null)
        );
    }

    private Core buildCore(List<Integer> cpuIds) throws IOException {
        List<List<Integer>> alreadyCheckedCpuIds = new ArrayList<>();

        return new Core(
            getCoreId(cpuIds.getFirst()),
            cpuIds.stream().map(cpuId -> {
                alreadyCheckedCpuIds.add(List.of(cpuId));
                return buildCPU(cpuId);
            }).toList(),
            getFilteredCaches(cpuIds, alreadyCheckedCpuIds)
        );
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

    private Map<Integer, String> getModelNames() {
        Map<Integer, String> modelNames = new HashMap<>();

        try (
            BufferedReader reader = Files.newBufferedReader(cpuInfoFile);
        ) {
            while (true) {
                Optional<String> cpuId = getCPUInfoValue("processor", reader);
                if (cpuId.isEmpty()) {
                    break;
                }

                Optional<String> modelName = getCPUInfoValue("model name", reader);
                if (modelName.isEmpty()) {
                    return Map.of();  // The cpuinfo file wasn't formatted as expected
                }

                modelNames.put(Integer.valueOf(cpuId.orElseThrow()), modelName.orElseThrow());
            }
        } catch (IOException | NumberFormatException _) {
            return Map.of();
        }

        return modelNames;
    }

    private Optional<String> getCPUInfoValue(String key, BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] splitLines = line.split(":", 2);

            if (splitLines.length == 2 && splitLines[0].stripTrailing().equals(key)) {
                return Optional.of(splitLines[1].stripLeading());
            }
        }

        return Optional.empty();
    }

    // TODO: Reduce cognitive load and improve performance
    //       by pivoting to sets instead of lists
    private Set<Cache> getFilteredCaches(List<Integer> cpuIds, List<List<Integer>> alreadyCheckedCpuIds) {
        Set<Cache> caches = new HashSet<>();

        try {
            for (int cpuId : cpuIds) {
                for (Cache cache : getCaches(cpuId)) {
                    if (cpuIds.containsAll(cache.cpuIds())) {
                        boolean skip = false;

                        for (List<Integer> alreadyCheckedCpuIdList : alreadyCheckedCpuIds) {
                            if (alreadyCheckedCpuIdList.containsAll(cache.cpuIds())) {
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
        } catch (IOException | NumberFormatException _) {
            return Set.of();
        }

        return caches;
    }

    private List<Cache> getCaches(int cpuId) throws IOException {
        try (
            Stream<Path> paths = Files.list(pathResolver.resolve(SYS_CPU_CACHE_DIR_TEMPLATE, cpuId));
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
        Path cacheDir = pathResolver.resolve(SYS_CPU_CACHE_DIR_TEMPLATE, cpuId);

        return new Cache(
            Integer.parseInt(readFile(cacheDir.resolve(index, "id"))),
            Integer.parseInt(readFile(cacheDir.resolve(index, "level"))),
            readFile(cacheDir.resolve(index, "type")),
            parseSize(readFile(cacheDir.resolve(index, "size"))),
            parseCPUList(readFile(cacheDir.resolve(index, "shared_cpu_list")))
        );
    }

    private int getCoreId(int cpuId) throws IOException {
        return Integer.parseInt(readFile(pathResolver.resolve(SYS_CPU_TOPO_CORE_ID_TEMPLATE, cpuId)));
    }

    private int getClusterId(int cpuId) throws IOException {
        return Integer.parseInt(readFile(pathResolver.resolve(SYS_CPU_TOPO_CLUSTER_ID_TEMPLATE, cpuId)));
    }

    private int getDieId(int cpuId) throws IOException {
        return Integer.parseInt(readFile(pathResolver.resolve(SYS_CPU_TOPO_DIE_ID_TEMPLATE, cpuId)));
    }

    private int getPackageId(int cpuId) throws IOException {
        return Integer.parseInt(readFile(pathResolver.resolve(SYS_CPU_TOPO_PACKAGE_ID_TEMPLATE, cpuId)));
    }

    private List<Integer> getCoreCPUs(int cpuId) throws IOException {
        return parseCPUList(readFile(pathResolver.resolve(SYS_CPU_TOPO_CORE_CPUS_TEMPLATE, cpuId)));
    }

    private List<Integer> getClusterCPUs(int cpuId) throws IOException {
        return parseCPUList(readFile(pathResolver.resolve(SYS_CPU_TOPO_CLUSTER_CPUS_TEMPLATE, cpuId)));
    }

    private List<Integer> getDieCPUs(int cpuId) throws IOException {
        return parseCPUList(readFile(pathResolver.resolve(SYS_CPU_TOPO_DIE_CPUS_TEMPLATE, cpuId)));
    }

    private List<Integer> getPackageCPUs(int cpuId) throws IOException {
        return parseCPUList(readFile(pathResolver.resolve(SYS_CPU_TOPO_PACKAGE_CPUS_TEMPLATE, cpuId)));
    }

    private List<Integer> getPresentCPUs() throws IOException {
        return parseCPUList(readFile(presentCPUsFile));
    }

    private List<Integer> getOnlineCPUs() throws IOException {
        return parseCPUList(readFile(onlineCPUsFile));
    }

    private List<Integer> getOfflineCPUs() throws IOException {
        return parseCPUList(readFile(offlineCPUsFile));
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
