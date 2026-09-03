package com.cadw.automation.driver;

import org.openqa.selenium.WebDriver;

public final class DriverSession {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<ExecutionTarget> TARGET = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();

    private DriverSession() {
    }

    static void set(WebDriver driver, ExecutionTarget target, Browser browser) {
        if (DRIVER.get() != null) {
            throw new IllegalStateException("A WebDriver session already exists on this thread");
        }
        DRIVER.set(driver);
        TARGET.set(target);
        BROWSER.set(browser);
    }

    public static WebDriver driver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver has not been initialized on this thread");
        }
        return driver;
    }

    public static WebDriver driverOrNull() {
        return DRIVER.get();
    }

    public static ExecutionTarget targetOrNull() {
        return TARGET.get();
    }

    public static Browser browserOrNull() {
        return BROWSER.get();
    }

    static void clear() {
        DRIVER.remove();
        TARGET.remove();
        BROWSER.remove();
    }
}
