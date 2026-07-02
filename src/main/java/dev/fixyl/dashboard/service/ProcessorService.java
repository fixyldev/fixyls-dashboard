package dev.fixyl.dashboard.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dev.fixyl.dashboard.dto.processor.CacheDTO;
import dev.fixyl.dashboard.dto.processor.FrequencyDTO;
import dev.fixyl.dashboard.dto.processor.ProcessorDTO;
import dev.fixyl.dashboard.service.provider.CacheProvider;
import dev.fixyl.dashboard.service.provider.FrequencyProvider;
import dev.fixyl.dashboard.service.provider.ProcessorProvider;
import dev.fixyl.dashboard.service.provider.TopologyProvider;
import dev.fixyl.dashboard.service.provider.CacheProvider.Cache;
import dev.fixyl.dashboard.sse.SseEmitterRegistry;

@Service
public class ProcessorService {

    private static final long INTERVAL_MILLIS = 1000L;
    private static final String EVENT_NAME = "processor";

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final SseEmitterRegistry registry;
    private final ProcessorProvider processorProvider;
    private final TopologyProvider topologyProvider;
    private final CacheProvider cacheProvider;
    private final FrequencyProvider frequencyProvider;

    private List<Integer> activeCPUs = List.of();
    private @Nullable String modelName;
    private @Nullable Integer socketCount;
    private @Nullable Integer coreCount;
    private int threadCount;
    private @Nullable CacheDTO cache;
    private @Nullable Long baseFrequency;
    private @Nullable Long maxFrequency;
    private @Nullable Long minFrequency;

    public ProcessorService(
        SseEmitterRegistry registry,
        ProcessorProvider processorProvider,
        TopologyProvider topologyProvider,
        CacheProvider cacheProvider,
        FrequencyProvider frequencyProvider
    ) {
        this.registry = registry;
        this.processorProvider = processorProvider;
        this.topologyProvider = topologyProvider;
        this.cacheProvider = cacheProvider;
        this.frequencyProvider = frequencyProvider;
    }

    @Scheduled(fixedRate = INTERVAL_MILLIS)
    private void tick() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }

        try {
            update();
        } finally {
            isRunning.lazySet(false);
        }
    }

    private void update() {
        List<Integer> activeCPUsNow = processorProvider.getCPUs();

        if (!activeCPUs.equals(activeCPUsNow)) {
            activeCPUs = activeCPUsNow;
            updateAll();
        }

        registry.broadcast(EVENT_NAME, buildProcessorDTO());
    }

    private @Nullable ProcessorDTO buildProcessorDTO() {
        if (activeCPUs.isEmpty()) {
            return null;
        }

        return new ProcessorDTO(
            modelName,
            socketCount,
            coreCount,
            threadCount,
            cache,
            buildFrequencyDTO()
        );
    }

    private @Nullable FrequencyDTO buildFrequencyDTO() {
        Map<Integer, Long> currentFrequencies = getCurrentFrequencies();

        if (
            ObjectUtils.allNull(baseFrequency, maxFrequency, minFrequency)
            && currentFrequencies.isEmpty()
        ) {
            return null;
        }

        return new FrequencyDTO(
            baseFrequency,
            maxFrequency,
            minFrequency,
            currentFrequencies
        );
    }

    private Map<Integer, Long> getCurrentFrequencies() {
        Map<Integer, Long> curFreqs = new HashMap<>();

        for (int cpuId : activeCPUs) {
            OptionalLong cpuCurFreq = frequencyProvider.getCurrentFrequency(cpuId);

            if (cpuCurFreq.isEmpty()) {
                // We don't want to push partial data (all or nothing)
                return Map.of();
            }

            curFreqs.put(cpuId, cpuCurFreq.orElseThrow());
        }

        return curFreqs;
    }

    private void updateAll() {
        updateModelName();
        updateSocketCount();
        updateCoreCount();
        updateThreadCount();
        updateCache();
        updateBaseFrequency();
        updateMaxFrequency();
        updateMinFrequency();
    }

    private void updateModelName() {
        this.modelName = processorProvider.getModelName().orElse(null);
    }

    private void updateSocketCount() {
        Set<Integer> packageIds = new HashSet<>();

        for (int cpuId : activeCPUs) {
            OptionalInt cpuPackageId = topologyProvider.getPackageId(cpuId);

            if (cpuPackageId.isEmpty()) {
                this.socketCount = null;
                return;
            }

            packageIds.add(cpuPackageId.orElseThrow());
        }

        this.socketCount = packageIds.size();
    }

    private void updateCoreCount() {
        Set<Integer> coreIds = new HashSet<>();

        for (int cpuId : activeCPUs) {
            OptionalInt cpuCoreId = topologyProvider.getCoreId(cpuId);

            if (cpuCoreId.isEmpty()) {
                this.coreCount = null;
                return;
            }

            coreIds.add(cpuCoreId.orElseThrow());
        }

        this.coreCount = coreIds.size();
    }

    private void updateThreadCount() {
        this.threadCount = activeCPUs.size();
    }

    private void updateCache() {
        Set<Cache> totalCache = new HashSet<>();
        for (Integer cpuId : activeCPUs) {
            Set<Cache> cpuCache = cacheProvider.getCache(cpuId);

            if (cpuCache.isEmpty()) {
                // Cache for this CPU couldn't be provided so
                // we cancel further cache retrieval and just conclude
                // that we don't have information about cache.
                this.cache = null;
                return;
            }

            totalCache.addAll(cpuCache);
        }

        Map<Integer, Long> cacheLevels = new HashMap<>();
        for (Cache cacheInstance : totalCache) {
            cacheLevels.put(
                cacheInstance.level(),
                cacheLevels.getOrDefault(
                    cacheInstance.level(),
                    0L
                ) + cacheInstance.size()
            );
        }

        this.cache = new CacheDTO(
            cacheLevels.get(1),
            cacheLevels.get(2),
            cacheLevels.get(3),
            cacheLevels.get(4)
        );
    }

    private void updateBaseFrequency() {
        long totalBaseFreq = Long.MIN_VALUE;

        for (int cpuId : activeCPUs) {
            OptionalLong cpuBaseFreq = frequencyProvider.getBaseFrequency(cpuId);

            if (cpuBaseFreq.isEmpty()) {
                this.baseFrequency = null;
                return;
            }

            totalBaseFreq = Long.max(totalBaseFreq, cpuBaseFreq.orElseThrow());
        }

        this.baseFrequency = totalBaseFreq;
    }

    private void updateMaxFrequency() {
        long totalMaxFreq = Long.MIN_VALUE;

        for (int cpuId : activeCPUs) {
            OptionalLong cpuMaxFreq = frequencyProvider.getMaxFrequency(cpuId);

            if (cpuMaxFreq.isEmpty()) {
                this.maxFrequency = null;
                return;
            }

            totalMaxFreq = Long.max(totalMaxFreq, cpuMaxFreq.orElseThrow());
        }

        this.maxFrequency = totalMaxFreq;
    }

    private void updateMinFrequency() {
        long totalMinFreq = Long.MAX_VALUE;

        for (int cpuId : activeCPUs) {
            OptionalLong cpuMinFreq = frequencyProvider.getMinFrequency(cpuId);

            if (cpuMinFreq.isEmpty()) {
                this.minFrequency = null;
                return;
            }

            totalMinFreq = Long.min(totalMinFreq, cpuMinFreq.orElseThrow());
        }

        this.minFrequency = totalMinFreq;
    }

}
