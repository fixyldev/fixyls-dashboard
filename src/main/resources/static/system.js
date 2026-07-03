import { addSseListener } from "./update.js";

const EVENT_NAME = "system";

addSseListener(EVENT_NAME, update);

function update(data) {

}
