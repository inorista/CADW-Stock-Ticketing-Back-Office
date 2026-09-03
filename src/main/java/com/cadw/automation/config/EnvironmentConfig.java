package com.cadw.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Reads one of the three explicitly allowed environment configuration files.
 */
public final class EnvironmentConfig {
    private static final String ENVIRONMENT_PROPERTY = "test.environment";
    private static final String ENVIRONMENT_VARIABLE = "TEST_ENVIRONMENT";
    private static final String DEFAULT_ENVIRONMENT = "dev";

    private static final Map<String, String> ALLOWED_FILES = Map.of(
            "dev", "config/dev.config",
            "staging", "config/staging.config",
            "prod", "config/prod.config");

    private final String environment;
    private final Properties values;

    private EnvironmentConfig(String environment, Properties values) {
        this.environment = environment;
        this.values = values;
    }

    /**
     * Loads the environment selected by -Dtest.environment or TEST_ENVIRONMENT.
     * Defaults to dev when neither is provided.
     */
    public static EnvironmentConfig active() {
        String selectedEnvironment = firstNonBlank(
                System.getProperty(ENVIRONMENT_PROPERTY),
                System.getenv(ENVIRONMENT_VARIABLE),
                DEFAULT_ENVIRONMENT);
        return load(selectedEnvironment);
    }

    /**
     * Loads exactly one allowlisted file: dev.config, staging.config, or
     * prod.config.
     */
    public static EnvironmentConfig load(String environment) {
        String normalizedEnvironment = normalize(environment);
        String resourceName = ALLOWED_FILES.get(normalizedEnvironment);
        if (resourceName == null) {
            throw new IllegalArgumentException(
                    "Unsupported environment '" + environment
                            + "'. Allowed values: " + ALLOWED_FILES.keySet());
        }

        Properties properties = new Properties();
        try (InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new IllegalStateException("Configuration file not found: " + resourceName);
            }
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read configuration: " + resourceName, exception);
        }

        return new EnvironmentConfig(normalizedEnvironment, properties);
    }

    public String environment() {
        return environment;
    }

    public String getString(String key) {
        return optionalString(key).orElseThrow(
                () -> new IllegalArgumentException(
                        "Missing configuration key '" + key + "' in " + environment + ".config"));
    }

    public String getString(String key, String defaultValue) {
        return optionalString(key).orElse(defaultValue);
    }

    public Optional<String> optionalString(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            String environmentKey = key.toUpperCase(Locale.ROOT)
                    .replace('.', '_')
                    .replace('-', '_');
            value = System.getenv(environmentKey);
        }
        if (value == null || value.isBlank()) {
            value = values.getProperty(key);
        }
        return value == null || value.isBlank()
                ? Optional.empty()
                : Optional.of(value.trim());
    }

    public int getInt(String key) {
        return parseInt(key, getString(key));
    }

    public int getInt(String key, int defaultValue) {
        return optionalString(key).map(value -> parseInt(key, value)).orElse(defaultValue);
    }

    public boolean getBoolean(String key) {
        return parseBoolean(key, getString(key));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return optionalString(key).map(value -> parseBoolean(key, value)).orElse(defaultValue);
    }

    public String baseUrl() {
        return getString("base.url");
    }

    public String browser() {
        return getString("webdriver.browser");
    }

    public boolean isHeadless() {
        return getBoolean("webdriver.headless");
    }

    public boolean acceptInsecureCertificates() {
        return getBoolean("webdriver.accept-insecure-certificates");
    }

    public int windowWidth() {
        return getInt("webdriver.window.width");
    }

    public int windowHeight() {
        return getInt("webdriver.window.height");
    }

    public int implicitWaitSeconds() {
        return getInt("webdriver.timeout.implicit-wait.seconds", 0);
    }

    public int explicitWaitSeconds() {
        return getInt("webdriver.timeout.explicit-wait.seconds");
    }

    public int pageLoadTimeoutSeconds() {
        return getInt("webdriver.timeout.page-load.seconds");
    }

    public int scriptTimeoutSeconds() {
        return getInt("webdriver.timeout.script.seconds");
    }

    private static int parseInt(String key, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Configuration key '" + key + "' must be an integer, but was '" + value + "'",
                    exception);
        }
    }

    private static boolean parseBoolean(String key, String value) {
        value = value.toLowerCase(Locale.ROOT);
        if (!"true".equals(value) && !"false".equals(value)) {
            throw new IllegalArgumentException(
                    "Configuration key '" + key + "' must be true or false, but was '" + value + "'");
        }
        return Boolean.parseBoolean(value);
    }

    private static String normalize(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        return environment.trim().toLowerCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        throw new IllegalStateException("No environment was configured");
    }
}
