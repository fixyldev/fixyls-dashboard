const sseUrl = "/api/sse";
const eventSource = new EventSource(sseUrl);

export function registerSseListener(eventName, eventListener) {
    eventSource.addEventListener(eventName, event => {
        eventListener(JSON.parse(event.data));
    });
}
