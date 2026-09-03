package com.cadw.automation.driver;

import java.util.Locale;

public enum ExecutionTarget {
    LOCAL,
    GRID,
    LAMBDATEST;

    public static ExecutionTarget from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "Unsupported execution target '" + value + "'. Use local, grid, or lambdatest.", exception);
        }
    }
}
