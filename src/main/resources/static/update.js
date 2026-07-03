import { secondsToString } from "./data.js";

const TICK_INTERVAL_MILLIS = 1000;

const sseUrl = "/api/sse";
const eventSource = new EventSource(sseUrl);

let currentBootTime = Number.NaN;

export function addSseListener(eventName, eventListener) {
    eventSource.addEventListener(eventName, event => {
        eventListener(JSON.parse(event.data));
    });
}

eventSource.addEventListener("systemInit", (event) => {
    systemInit(JSON.parse(event.data));
})

eventSource.addEventListener("processorUpdate", (event) => {
    processorUpdate(JSON.parse(event.data));
});

function systemInit(data) {
    currentBootTime = data.bootTime ?? Number.NaN;
}

function processorUpdate(data) {
    Object.entries(data.currentFrequencies).forEach(([cpuId, frequency]) => {
        document.getElementById(`cpu${cpuId}`).textContent = frequency;
    });
}

function getUptimeSeconds(bootTimeSeconds) {
    const nowSeconds = Math.floor(Date.now() / 1000)
    return Math.abs(nowSeconds - bootTimeSeconds);
}

function tick() {
    if (Number.isNaN(currentBootTime)) {
        document.getElementById("system-uptime").textContent = "N/A";
    } else {
        document.getElementById("system-uptime").textContent = secondsToString(getUptimeSeconds(currentBootTime));
    }
}

setInterval(tick, TICK_INTERVAL_MILLIS);
