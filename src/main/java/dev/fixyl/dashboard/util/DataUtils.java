package dev.fixyl.dashboard.util;

import java.text.DecimalFormat;
import java.util.List;

public final class DataUtils {

    private static final String NAN = "NaN";
    private static final double BASE_2_MULTIPLIER = 1024.0;
    private static final double BASE_10_MULTIPLIER = 1000.0;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    private static final List<String> BYTE_UNITS = List.of(
        "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"
    );
    private static final List<String> HERTZ_UNITS = List.of(
        "Hz", "kHz", "MHz", "GHz", "THz", "PHz", "EHz"
    );

    private DataUtils() {}

    public static String bytesToString(long bytes) {
        return reduce(bytes, BYTE_UNITS, BASE_2_MULTIPLIER);
    }

    public static String hertzToString(long hertz) {
        return reduce(hertz, HERTZ_UNITS, BASE_10_MULTIPLIER);
    }

    private static String reduce(long data, List<String> units, double multiplier) {
        double reducedData = data;

        for (String unit : units) {
            if (reducedData < multiplier) {
                return DECIMAL_FORMAT.format(reducedData) + " " + unit;
            }

            reducedData /= multiplier;
        }

        // This should never run
        return NAN;
    }

}
