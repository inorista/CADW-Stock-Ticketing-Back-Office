package com.cadw.automation.utils;

import java.util.Optional;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public final class ScreenshotUtils {
    private ScreenshotUtils() {
    }

    public static Optional<byte[]> capture(WebDriver driver) {
        if (driver instanceof TakesScreenshot screenshotDriver) {
            try {
                return Optional.of(screenshotDriver.getScreenshotAs(OutputType.BYTES));
            } catch (RuntimeException ignored) {
                // Reporting must never hide the original test failure.
            }
        }
        return Optional.empty();
    }
}
