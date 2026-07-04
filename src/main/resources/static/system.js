import { secondsToString } from "./data.js";
import { Data, LocallyProcessedData, setElementVisibility, Subheader } from "./elements.js";
import { registerSseListener } from "./sse.js";

const EVENT_NAME = "system";
const TICK_INTERVAL_MILLIS = 1000;

// subheader
const hostname = new Subheader("system-hostname");

// group - status
const uptime = new LocallyProcessedData("system-status-uptime", uptimeProcessor);
const shutdown = new LocallyProcessedData("system-status-shutdown", shutdownProcessor);

// group - info
const os = new Data("system-info-os");
const kernel = new Data("system-info-kernel");

let tickingAlreadyStarted = false;

registerSseListener(EVENT_NAME, update);

function update(data) {
    const visible = data !== null;
    setElementVisibility("system", visible);
    if (!visible) { return; }

    hostname.update(data.hostname);
    updateInfo(data);
    updateStatus(data);

    if (!tickingAlreadyStarted) {
        startTicking();
        tickingAlreadyStarted = true;
    }
}

function updateStatus(data) {
    const visible = (
        data.bootTime !== null
        || data.shutdown !== null
    );

    setElementVisibility("system-status", visible);
    if (!visible) { return; }

    uptime.setBaseData(data.bootTime);
    shutdown.setBaseData(data.shutdown);
}

function updateInfo(data) {
    const visible = (
        data.os !== null
        || data.kernel !== null
    );

    setElementVisibility("system-info", visible);
    if (!visible) { return; }

    os.update(data.os);
    kernel.update(data.kernel);
}

function uptimeProcessor(bootTime) {
    const nowSeconds = Math.floor(Date.now() / 1000);
    const timeDiff = Math.abs(nowSeconds - bootTime);
    return secondsToString(timeDiff);
}

function shutdownProcessor(shutdownStruct) {
    let value = "";

    if (shutdownStruct.shutdownTime !== null) {
        const shutdownMillis = Math.floor(shutdownStruct.shutdownTime / 1000);
        const timeDiff = Math.abs(shutdownMillis - Date.now());
        value += secondsToString(Math.floor(timeDiff / 1000)) + " - ";
    }

    value += shutdownStruct.mode;

    if (shutdownStruct.message !== null) {
        value += ": " + shutdownStruct.message;
    }

    return value;
}

function startTicking() {
    tick();
    setInterval(tick, TICK_INTERVAL_MILLIS);
}

function tick() {
    uptime.update();
    shutdown.update();
}
