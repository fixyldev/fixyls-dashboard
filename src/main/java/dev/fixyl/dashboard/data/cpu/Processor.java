package dev.fixyl.dashboard.data.cpu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

import dev.fixyl.dashboard.util.DataUtils;

public class Processor {

    private static final int AVERAGE_CPU_COUNT = 16;

    private final List<Package> packages;

    private final List<CPU> cpus;
    private final List<Cache> caches;

    private final @Nullable String name;
    private final int socketCount;
    private final int coreCount;
    private final int threadCount;
    private final Map<Integer, Long> cacheSizes;
    private final @Nullable String baseFrequency;
    private final @Nullable String maxFrequency;
    private final @Nullable String minFrequency;

    public Processor(List<Package> packages) {
        this.packages = new ArrayList<>(packages);

        this.cpus = gatherCPUs(this.packages);
        this.caches = gatherCaches(this.packages);

        this.name = buildName(this.cpus);
        this.socketCount = this.packages.size();
        this.coreCount = countCores(this.packages);
        this.threadCount = this.cpus.size();
        this.cacheSizes = calculateCacheSizes(this.caches);
        this.baseFrequency = buildBaseFrequency(this.cpus);
        this.maxFrequency = buildMaxFrequency(this.cpus);
        this.minFrequency = buildMinFrequency(this.cpus);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    public int getSocketCount() {
        return this.socketCount;
    }

    public int getCoreCount() {
        return this.coreCount;
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    public Optional<Long> getCacheSize(int level) {
        return Optional.ofNullable(this.cacheSizes.get(level));
    }

    public Optional<String> getReadableCacheSize(int level) {
        Long size = this.cacheSizes.get(level);

        if (size != null) {
            return Optional.of(DataUtils.bytesToString(size));
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getBaseFrequency() {
        return Optional.ofNullable(this.baseFrequency);
    }

    public Optional<String> getMaxFrequency() {
        return Optional.ofNullable(this.maxFrequency);
    }

    public Optional<String> getMinFrequency() {
        return Optional.ofNullable(this.minFrequency);
    }

    private static List<CPU> gatherCPUs(List<Package> packages) {
        List<CPU> accumulator = new ArrayList<>(AVERAGE_CPU_COUNT);

        for (Package cpuPackage : packages) {
            cpuPackage.gatherCPUs(accumulator);
        }

        return accumulator;
    }

    private static List<Cache> gatherCaches(List<Package> packages) {
        List<Cache> accumulator = new ArrayList<>();

        for (Package cpuPackage : packages) {
            cpuPackage.gatherCaches(accumulator);
        }

        return accumulator;
    }

    private static @Nullable String buildName(List<CPU> cpus) {
        String name = cpus.stream()
            .map(CPU::modelName)
            .filter(StringUtils::hasText)
            .distinct()
            .collect(Collectors.joining(", "));

        return (name.isBlank()) ? null : name;
    }

    private static int countCores(List<Package> packages) {
        return packages.stream().mapToInt(Package::countCores).sum();
    }

    // Suppress null safety warning for Long unboxing for Long::sum
    // We know for sure that no value there can ever be null
    @SuppressWarnings("null")
    private static Map<Integer, Long> calculateCacheSizes(List<Cache> caches) {
        Map<Integer, Long> cacheSizes = new HashMap<>();

        for (Cache cache : caches) {
            cacheSizes.merge(cache.level(), cache.size(), Long::sum);
        }

        return cacheSizes;
    }

    // We explicitly filter for non-null elements,
    // DataUtils::hertzToString cannot throw
    @SuppressWarnings("null")
    private static @Nullable String buildBaseFrequency(List<CPU> cpus) {
        String baseFrequency = cpus.stream()
            .map(CPU::baseFrequency)
            .filter(Objects::nonNull)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .map(DataUtils::hertzToString)
            .collect(Collectors.joining(", "));

        return (baseFrequency.isBlank()) ? null : baseFrequency;
    }

    private static @Nullable String buildMaxFrequency(List<CPU> cpus) {
        OptionalLong maxFrequency = cpus.stream()
            .map(CPU::maxFrequency)
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .max();

        if (maxFrequency.isEmpty()) {
            return null;
        }

        return DataUtils.hertzToString(maxFrequency.orElseThrow());
    }

    private static @Nullable String buildMinFrequency(List<CPU> cpus) {
        OptionalLong minFrequency = cpus.stream()
            .map(CPU::minFrequency)
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .min();

        if (minFrequency.isEmpty()) {
            return null;
        }

        return DataUtils.hertzToString(minFrequency.orElseThrow());
    }

}
