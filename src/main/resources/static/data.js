const BASE_2_MULTIPLIER = 1024;
const BASE_10_MULTIPLIER = 1000;

const TIME_UNITS = ["s", "m", "h", "d"];
const DATA_UNITS = ["B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"];
const FREQ_UNITS = ["Hz", "kHz", "MHz", "GHz", "THz", "PHz", "EHz"];

const formatter = new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 });

export function secondsToString(seconds) {
    seconds = Number(seconds);

    let s = 0;
    let m = 0;
    let h = 0;
    let d = 0;

    m = Math.floor(seconds / 60);
    s = seconds % 60;

    h = Math.floor(m / 60);
    m = m % 60;

    d = Math.floor(h / 24);
    h = h % 24;

    let time = "";
    if (d) { time += d + TIME_UNITS[3] + " "; }
    if (h || d) { time += h + TIME_UNITS[2] + " "; }
    if (m || h || d) { time += m + TIME_UNITS[1] + " "; }
    time += s + TIME_UNITS[0];

    return time;
}

export function bytesToString(bytes) {
    return reduce(Number(bytes), DATA_UNITS, BASE_2_MULTIPLIER);
}

export function hertzToString(hertz) {
    return reduce(Number(hertz), FREQ_UNITS, BASE_10_MULTIPLIER);
}

function reduce(data, units, multiplier) {
    let reducedData = data;

    for (const unit of units) {
        if (reducedData < multiplier) {
            return formatter.format(reducedData) + " " + unit;
        }

        reducedData /= multiplier;
    }

    // This should never run
    return String(Number.NaN);
}
