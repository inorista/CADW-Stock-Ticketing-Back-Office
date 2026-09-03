package com.cadw.automation.driver;

import java.util.Locale;

public enum Browser {
    CHROME,
    FIREFOX,
    EDGE;

    public static Browser from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "Unsupported browser '" + value + "'. Use chrome, firefox, or edge.", exception);
        }
    }
}
