package dev.fixyl.dashboard.util;

import java.util.Optional;

public final class ParseUtils {

    private ParseUtils() {}

    public static Optional<Integer> parseIntOrEmpty(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException _) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLongOrEmpty(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException _) {
            return Optional.empty();
        }
    }

}
