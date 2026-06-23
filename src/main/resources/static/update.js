const sseUrl = "/api/sse";
const eventSource = new EventSource(sseUrl);

eventSource.addEventListener("processorUpdate", (event) => {
    processorUpdate(JSON.parse(event.data));
});

function processorUpdate(data) {
    Object.entries(data.currentFrequencies).forEach(([cpuId, frequency]) => {
        document.querySelector(`#cpu${cpuId} > .value`).textContent = frequency;
    });
}
