const MISSING_DATA = "N/A";
const HIDDEN_CLASS = "hidden";

export function getValueElementById(id) {
    return document.querySelector(`#${id} > .value`);
}

export function setElementVisibility(id, visible) {
    const element = document.getElementById(id);

    if (visible) {
        element.classList.remove(HIDDEN_CLASS);
    } else {
        element.classList.add(HIDDEN_CLASS);
    }
}

export class Subheader {

    #id;
    #value;

    constructor(id) {
        this.#id = id;
    }

    update(newValue) {
        const visible = newValue !== null;
        setElementVisibility(this.#id, visible);
        if (!visible || newValue === this.#value) {
            return;
        }

        document.getElementById(this.#id).textContent = newValue;
        this.#value = newValue;
    }

}

export class Data {

    #id;
    #value;
    #valueMapper;

    constructor(id, valueMapper = value => value) {
        this.#id = id;
        this.#valueMapper = valueMapper;
    }

    update(newValue) {
        const visible = newValue !== null;
        setElementVisibility(this.#id, visible);
        if (!visible || newValue === this.#value) {
            return;
        }

        getValueElementById(this.#id).textContent = this.#valueMapper(newValue);
        this.#value = newValue;
    }

}

export class DynamicData {

    #id;
    #keys = [];
    #keyToLabel;
    #valueMapper;

    constructor(id, keyToLabel = key => key, valueMapper = value => value) {
        this.#id = id;
        this.#keyToLabel = keyToLabel;
        this.#valueMapper = valueMapper;
    }

    update(newData) {
        const newKeys = Object.keys(newData ?? {});
        const visible = newKeys.length !== 0;
        setElementVisibility(this.#id, visible);
        if (!visible) { return; }

        if (!arraysEquals(newKeys, this.#keys)) {
            this.#updateLabels(newKeys);
            this.#keys = newKeys;
        }

        this.#updateValues(Object.values(newData));
    }

    #updateLabels(newKeys) {
        const container = document.getElementById(this.#id);
        let difference = newKeys.length - this.#keys.length;

        for (; difference > 0; difference--) {
            container.appendChild(createDataElement());
        }
        for (; difference < 0; difference++) {
            container.querySelector(":scope > .data")?.remove();
        }

        const elements = container.querySelectorAll(`:scope > .data > .label`);
        const size = Math.min(elements.length, newKeys.length);

        for (let index = 0; index < size; index++) {
            elements[index].textContent = this.#keyToLabel(newKeys[index]);
        }
    }

    #updateValues(newValues) {
        const elements = document.querySelectorAll(`#${this.#id} > .data > .value`);
        const size = Math.min(elements.length, newValues.length);

        for (let index = 0; index < size; index++) {
            elements[index].textContent = this.#valueMapper(newValues[index]);
        }
    }

}

function createDataElement() {
    const labelElement = document.createElement("p");
    labelElement.classList.add("label");
    labelElement.textContent = MISSING_DATA;
    const valueElement = document.createElement("p");
    valueElement.classList.add("value");
    valueElement.textContent = MISSING_DATA;

    const dataElement = document.createElement("div");
    dataElement.classList.add("data");
    dataElement.appendChild(labelElement);
    dataElement.appendChild(valueElement);

    return dataElement;
}

function arraysEquals(array1, array2) {
    return array1.length === array2.length
        && array1.every((value, index) => value === array2[index]);
}
