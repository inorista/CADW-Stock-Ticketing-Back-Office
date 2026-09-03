package com.cadw.automation.driver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.state.BrowserStateManager;

public final class DriverFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver start(String browserValue, String targetValue, String testName) {
        EnvironmentConfig config = EnvironmentConfig.active();
        Browser browser = Browser.from(firstNonBlank(browserValue, config.browser()));
        ExecutionTarget target = ExecutionTarget.from(
                firstNonBlank(targetValue, config.getString("execution")));

        WebDriver driver = switch (target) {
            case LOCAL -> createLocal(browser, config);
            case GRID -> createGrid(browser, config);
            case LAMBDATEST -> createLambdaTest(browser, config, testName);
        };

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.implicitWaitSeconds()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.pageLoadTimeoutSeconds()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(config.scriptTimeoutSeconds()));
        DriverSession.set(driver, target, browser);
        BrowserStateManager.restore(driver, config, browser);
        LOG.info("Started {} session on {} for {}", browser, target, testName);
        return driver;
    }

    public static void stop() {
        stop(true);
    }

    public static void stop(boolean saveBrowserState) {
        WebDriver driver = DriverSession.driverOrNull();
        Browser browser = DriverSession.browserOrNull();
        try {
            if (driver != null) {
                if (saveBrowserState && browser != null) {
                    BrowserStateManager.save(driver, EnvironmentConfig.active(), browser);
                }
                driver.quit();
            }
        } finally {
            DriverSession.clear();
        }
    }

    /** Saves the current authenticated state immediately, typically after a successful login. */
    public static boolean saveBrowserState() {
        WebDriver driver = DriverSession.driver();
        Browser browser = DriverSession.browserOrNull();
        if (browser == null) {
            throw new IllegalStateException("Browser has not been initialized on this thread");
        }
        return BrowserStateManager.save(driver, EnvironmentConfig.active(), browser);
    }

    /** Removes persisted state for the current environment, account, and browser. */
    public static boolean clearBrowserState() {
        Browser browser = DriverSession.browserOrNull();
        if (browser == null) {
            browser = Browser.from(EnvironmentConfig.active().browser());
        }
        return BrowserStateManager.clear(EnvironmentConfig.active(), browser);
    }

    /** Clears cookies and Web Storage in the active browser without deleting persisted state. */
    public static void resetCurrentBrowserSession() {
        WebDriver driver = DriverSession.driver();
        driver.get(EnvironmentConfig.active().baseUrl());
        driver.manage().deleteAllCookies();
        if (driver instanceof JavascriptExecutor executor) {
            executor.executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        }
        driver.get(EnvironmentConfig.active().baseUrl());
    }

    public static void markRemoteStatus(boolean passed, String reason) {
        WebDriver driver = DriverSession.driverOrNull();
        if (driver instanceof JavascriptExecutor executor
                && DriverSession.targetOrNull() == ExecutionTarget.LAMBDATEST) {
            executor.executeScript("lambda-status=" + (passed ? "passed" : "failed"));
            executor.executeScript("lambda-name=" + sanitize(reason));
        }
    }

    private static WebDriver createLocal(Browser browser, EnvironmentConfig config) {
        return switch (browser) {
            case CHROME -> new ChromeDriver(chromeOptions(config));
            case FIREFOX -> new FirefoxDriver(firefoxOptions(config));
            case EDGE -> new EdgeDriver(edgeOptions(config));
        };
    }

    private static WebDriver createGrid(Browser browser, EnvironmentConfig config) {
        MutableCapabilities capabilities = browserOptions(browser, config);
        setIfPresent(capabilities, "browserVersion", config.getString("browser.version", ""));
        setPlatform(capabilities, config.getString("platform", "ANY"));
        return remote(config.getString("remote.url"), capabilities);
    }

    private static WebDriver createLambdaTest(
            Browser browser, EnvironmentConfig config, String testName) {
        MutableCapabilities capabilities = browserOptions(browser, config);
        capabilities.setCapability("browserVersion", config.getString("lt.browser.version"));
        String username = config.getString("lt.username");
        String accessKey = config.getString("lt.access.key");

        Map<String, Object> options = new HashMap<>();
        options.put("platformName", config.getString("lt.platform"));
        options.put("project", config.getString("lt.project"));
        options.put("build", config.getString("lt.build"));
        options.put("name", testName);
        options.put("w3c", true);
        options.put("tunnel", config.getBoolean("lt.tunnel"));
        options.put("video", config.getBoolean("lt.video"));
        options.put("network", config.getBoolean("lt.network"));
        options.put("console", config.getBoolean("lt.console"));
        capabilities.setCapability("LT:Options", options);

        return remote(authenticatedUrl(config.getString("lt.grid.url"), username, accessKey), capabilities);
    }

    private static RemoteWebDriver remote(String url, Capabilities capabilities) {
        try {
            return remote(URI.create(url).toURL(), capabilities);
        } catch (MalformedURLException | IllegalArgumentException exception) {
            throw new IllegalStateException("Invalid remote WebDriver URL: " + url, exception);
        }
    }

    private static RemoteWebDriver remote(URL url, Capabilities capabilities) {
        return new RemoteWebDriver(url, capabilities);
    }

    private static URL authenticatedUrl(String url, String username, String accessKey) {
        try {
            URI base = URI.create(url);
            return new URI(
                    base.getScheme(),
                    username + ":" + accessKey,
                    base.getHost(),
                    base.getPort(),
                    base.getPath(),
                    base.getQuery(),
                    base.getFragment())
                    .toURL();
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException exception) {
            throw new IllegalStateException("Invalid LambdaTest grid URL: " + url, exception);
        }
    }

    private static MutableCapabilities browserOptions(Browser browser, EnvironmentConfig config) {
        return switch (browser) {
            case CHROME -> chromeOptions(config);
            case FIREFOX -> firefoxOptions(config);
            case EDGE -> edgeOptions(config);
        };
    }

    private static ChromeOptions chromeOptions(EnvironmentConfig config) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--window-size=" + config.windowWidth() + "," + config.windowHeight(),
                "--disable-dev-shm-usage",
                "--no-sandbox");
        options.setAcceptInsecureCerts(config.acceptInsecureCertificates());
        if (config.isHeadless()) {
            options.addArguments("--headless=new");
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions(EnvironmentConfig config) {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments(
                "--width=" + config.windowWidth(),
                "--height=" + config.windowHeight());
        options.setAcceptInsecureCerts(config.acceptInsecureCertificates());
        if (config.isHeadless()) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static EdgeOptions edgeOptions(EnvironmentConfig config) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments(
                "--window-size=" + config.windowWidth() + "," + config.windowHeight(),
                "--disable-dev-shm-usage",
                "--no-sandbox");
        options.setAcceptInsecureCerts(config.acceptInsecureCertificates());
        if (config.isHeadless()) {
            options.addArguments("--headless=new");
        }
        return options;
    }

    private static void setPlatform(MutableCapabilities capabilities, String platform) {
        if (platform != null && !platform.isBlank() && !"ANY".equalsIgnoreCase(platform)) {
            capabilities.setCapability("platformName", platform);
        }
    }

    private static void setIfPresent(MutableCapabilities capabilities, String key, String value) {
        if (value != null && !value.isBlank()) {
            capabilities.setCapability(key, value);
        }
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    private static String sanitize(String value) {
        return value == null ? "Test finished" : value.replaceAll("[\\r\\n]", " ");
    }
}
