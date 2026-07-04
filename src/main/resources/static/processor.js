import { bytesToString, hertzToString } from "./data.js";
import { Data, DynamicData, setElementVisibility, Subheader } from "./elements.js";
import { registerSseListener } from "./sse.js";

const EVENT_NAME = "processor";

// subheader
const modelName = new Subheader("processor-model-name");

// group - meta
const socketCount = new Data("processor-meta-socket-count");
const coreCount = new Data("processor-meta-core-count");
const threadCount = new Data("processor-meta-thread-count");
const baseFreq = new Data("processor-meta-base-freq", hertzToString);
const maxFreq = new Data("processor-meta-max-freq", hertzToString);
const minFreq = new Data("processor-meta-min-freq", hertzToString);

// group - cache
const cacheLevel1 = new Data("processor-cache-level-1", bytesToString);
const cacheLevel2 = new Data("processor-cache-level-2", bytesToString);
const cacheLevel3 = new Data("processor-cache-level-3", bytesToString);
const cacheLevel4 = new Data("processor-cache-level-4", bytesToString);

// group - freq
const currentFreqs = new DynamicData("processor-freq-current", key => `CPU-${key}`, hertzToString);

registerSseListener(EVENT_NAME, update);

function update(data) {
    const visible = data !== null;
    setElementVisibility("processor", visible);
    if (!visible) { return; }

    modelName.update(data.modelName);
    updateMeta(data);
    updateCache(data);
    updateFreq(data);
}

function updateMeta(data) {
    const visible = (
        data.socketCount !== null
        || data.coreCount !== null
        || data.threadCount !== null
        || (data.frequency?.base ?? null) !== null
        || (data.frequency?.max ?? null) !== null
        || (data.frequency?.min ?? null) !== null
    );

    setElementVisibility("processor-meta", visible);
    if (!visible) { return; }

    socketCount.update(data.socketCount);
    coreCount.update(data.coreCount);
    threadCount.update(data.threadCount);
    baseFreq.update(data.frequency?.base ?? null);
    maxFreq.update(data.frequency?.max ?? null);
    minFreq.update(data.frequency?.min ?? null);
}

function updateCache(data) {
    const visible = (
        (data.cache?.level1 ?? null) !== null
        || (data.cache?.level2 ?? null) !== null
        || (data.cache?.level3 ?? null) !== null
        || (data.cache?.level4 ?? null) !== null
    );

    setElementVisibility("processor-cache", visible);
    if (!visible) { return; }

    cacheLevel1.update(data.cache?.level1 ?? null);
    cacheLevel2.update(data.cache?.level2 ?? null);
    cacheLevel3.update(data.cache?.level3 ?? null);
    cacheLevel4.update(data.cache?.level4 ?? null);
}

function updateFreq(data) {
    const visible = (data.frequency?.current ?? null) !== null;
    setElementVisibility("processor-freq", visible);
    if (!visible) { return; }

    currentFreqs.update(data.frequency?.current ?? null);
}
