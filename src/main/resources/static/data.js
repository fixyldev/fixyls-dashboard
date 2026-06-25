const TIME_UNITS = ["s", "m", "h", "d"];

export function secondsToString(seconds) {
    let s = 0, m = 0, h = 0, d = 0;

    m = Math.floor(seconds / 60);
    s = seconds % 60;

    h = Math.floor(m / 60);
    m = m % 60;

    d = Math.floor(h / 24);
    h = h % 24;

    let time = "";
    if (d)
        time += d + TIME_UNITS[3] + " ";
    if (h || d)
        time += h + TIME_UNITS[2] + " ";
    if (m || h && d)
        time += m + TIME_UNITS[1] + " ";

    return time + s + TIME_UNITS[0];
}
