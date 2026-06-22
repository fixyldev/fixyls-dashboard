package dev.fixyl.dashboard.util;

import java.text.DecimalFormat;
import java.util.List;

public final class SizeUtils {

    private static final String NAN = "NaN";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    private static final List<String> UNITS = List.of(
        "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"
    );

    private SizeUtils() {}

    public static String bytesToString(long bytes) {
        double size = bytes;

        for (String unit : UNITS) {
            if (size < 1024.0) {
                return DECIMAL_FORMAT.format(size) + " " + unit;
            }

            size /= 1024.0;
        }

        // This should never return because nothing bigger
        // than EiB can fit into a long
        return NAN;
    }

}
