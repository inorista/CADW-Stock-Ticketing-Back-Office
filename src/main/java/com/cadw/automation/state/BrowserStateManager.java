package com.cadw.automation.state;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.Browser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Persists cookies and Web Storage so authenticated sessions can be reused. */
public final class BrowserStateManager {
    private static final Logger LOG = LoggerFactory.getLogger(BrowserStateManager.class);
    private static final String STATE_VERSION = "1";
    private static final Map<Path, Object> FILE_LOCKS = new ConcurrentHashMap<>();

    private BrowserStateManager() {
    }

    public static boolean restore(
            WebDriver driver, EnvironmentConfig config, Browser browser) {
        if (!config.getBoolean("auth.state.enabled", true)) {
            return false;
        }

        Path stateFile = stateFile(config, browser);
        synchronized (lockFor(stateFile)) {
            if (!Files.isRegularFile(stateFile) || isExpired(stateFile, config)) {
                return false;
            }

            try {
                Properties state = load(stateFile);
                if (!STATE_VERSION.equals(state.getProperty("metadata.version"))) {
                    LOG.warn("Ignoring unsupported browser state file: {}", stateFile);
                    return false;
                }

                String origin = origin(config.baseUrl());
                if (!origin.equalsIgnoreCase(state.getProperty("metadata.origin", ""))) {
                    LOG.warn("Ignoring browser state for a different origin: {}", stateFile);
                    return false;
                }
                driver.get(origin);
                restoreCookies(driver, state);
                restoreStorage(driver, state, "local", "window.localStorage");
                restoreStorage(driver, state, "session", "window.sessionStorage");
                driver.navigate().refresh();
                LOG.info("Restored browser authentication state from {}", stateFile);
                return true;
            } catch (IOException | RuntimeException exception) {
                LOG.warn("Could not restore browser state from {}", stateFile, exception);
                return false;
            }
        }
    }

    public static boolean save(
            WebDriver driver, EnvironmentConfig config, Browser browser) {
        if (!config.getBoolean("auth.state.enabled", true)) {
            return false;
        }

        Path stateFile = stateFile(config, browser);
        synchronized (lockFor(stateFile)) {
            try {
                String origin = origin(config.baseUrl());
                if (!sameOrigin(driver.getCurrentUrl(), origin)) {
                    driver.get(origin);
                }

                Properties state = new Properties();
                state.setProperty("metadata.version", STATE_VERSION);
                state.setProperty("metadata.environment", config.environment());
                state.setProperty("metadata.origin", origin);
                state.setProperty("metadata.saved-at", Instant.now().toString());
                writeCookies(state, driver.manage().getCookies());
                writeStorage(state, "local", readStorage(driver, "window.localStorage"));
                writeStorage(state, "session", readStorage(driver, "window.sessionStorage"));
                writeAtomically(stateFile, state);
                LOG.info("Saved browser authentication state to {}", stateFile);
                return true;
            } catch (IOException | RuntimeException exception) {
                LOG.warn("Could not save browser state to {}", stateFile, exception);
                return false;
            }
        }
    }

    public static boolean clear(EnvironmentConfig config, Browser browser) {
        Path stateFile = stateFile(config, browser);
        synchronized (lockFor(stateFile)) {
            try {
                boolean deleted = Files.deleteIfExists(stateFile);
                if (deleted) {
                    LOG.info("Deleted browser authentication state at {}", stateFile);
                }
                return deleted;
            } catch (IOException exception) {
                throw new IllegalStateException("Could not delete browser state: " + stateFile, exception);
            }
        }
    }

    private static Properties load(Path stateFile) throws IOException {
        Properties state = new Properties();
        try (InputStream input = Files.newInputStream(stateFile)) {
            state.load(input);
        }
        return state;
    }

    private static void writeCookies(Properties state, Set<Cookie> cookies) {
        state.setProperty("cookie.count", Integer.toString(cookies.size()));
        int index = 0;
        for (Cookie cookie : cookies) {
            String prefix = "cookie." + index++ + ".";
            put(state, prefix + "name", cookie.getName());
            put(state, prefix + "value", cookie.getValue());
            put(state, prefix + "domain", cookie.getDomain());
            put(state, prefix + "path", cookie.getPath());
            if (cookie.getExpiry() != null) {
                put(state, prefix + "expiry", Long.toString(cookie.getExpiry().getTime()));
            }
            put(state, prefix + "secure", Boolean.toString(cookie.isSecure()));
            put(state, prefix + "http-only", Boolean.toString(cookie.isHttpOnly()));
            put(state, prefix + "same-site", cookie.getSameSite());
        }
    }

    private static void restoreCookies(WebDriver driver, Properties state) {
        driver.manage().deleteAllCookies();
        int count = parseCount(state, "cookie.count");
        for (int index = 0; index < count; index++) {
            String prefix = "cookie." + index + ".";
            String name = state.getProperty(prefix + "name");
            String value = state.getProperty(prefix + "value");
            if (name == null || value == null) {
                continue;
            }

            Cookie.Builder builder = new Cookie.Builder(name, value);
            optional(state, prefix + "domain", builder::domain);
            optional(state, prefix + "path", builder::path);
            optional(state, prefix + "expiry", valueString ->
                    builder.expiresOn(new Date(Long.parseLong(valueString))));
            builder.isSecure(Boolean.parseBoolean(state.getProperty(prefix + "secure", "false")));
            builder.isHttpOnly(Boolean.parseBoolean(state.getProperty(prefix + "http-only", "false")));
            optional(state, prefix + "same-site", builder::sameSite);

            Cookie cookie = builder.build();
            if (cookie.getExpiry() == null || cookie.getExpiry().after(new Date())) {
                driver.manage().addCookie(cookie);
            }
        }
    }

    private static Map<String, String> readStorage(WebDriver driver, String storageName) {
        if (!(driver instanceof JavascriptExecutor executor)) {
            return Map.of();
        }
        Object result = executor.executeScript(
                "const result = {}; "
                        + "for (let i = 0; i < " + storageName + ".length; i++) { "
                        + "  const key = " + storageName + ".key(i); "
                        + "  result[key] = " + storageName + ".getItem(key); "
                        + "} return result;");
        if (!(result instanceof Map<?, ?> rawValues)) {
            return Map.of();
        }

        Map<String, String> values = new LinkedHashMap<>();
        rawValues.forEach((key, value) -> values.put(String.valueOf(key), String.valueOf(value)));
        return values;
    }

    private static void writeStorage(Properties state, String type, Map<String, String> values) {
        state.setProperty(type + ".count", Integer.toString(values.size()));
        int index = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String prefix = type + "." + index++ + ".";
            state.setProperty(prefix + "key", entry.getKey());
            state.setProperty(prefix + "value", entry.getValue());
        }
    }

    private static void restoreStorage(
            WebDriver driver, Properties state, String type, String storageName) {
        if (!(driver instanceof JavascriptExecutor executor)) {
            return;
        }

        Map<String, String> values = new LinkedHashMap<>();
        int count = parseCount(state, type + ".count");
        for (int index = 0; index < count; index++) {
            String prefix = type + "." + index + ".";
            String key = state.getProperty(prefix + "key");
            String value = state.getProperty(prefix + "value");
            if (key != null && value != null) {
                values.put(key, value);
            }
        }

        executor.executeScript(
                "const storage = " + storageName + "; storage.clear(); "
                        + "for (const [key, value] of Object.entries(arguments[0])) { "
                        + "  storage.setItem(key, value); "
                        + "}",
                values);
    }

    private static int parseCount(Properties state, String key) {
        try {
            return Integer.parseInt(state.getProperty(key, "0"));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid browser state count: " + key, exception);
        }
    }

    private static void writeAtomically(Path stateFile, Properties state) throws IOException {
        Path directory = stateFile.getParent();
        Files.createDirectories(directory);
        Path temporaryFile = Files.createTempFile(directory, stateFile.getFileName() + ".", ".tmp");
        try {
            restrictPermissions(temporaryFile);
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                state.store(output, "Selenium browser authentication state - keep private");
            }
            try {
                Files.move(
                        temporaryFile,
                        stateFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, stateFile, StandardCopyOption.REPLACE_EXISTING);
            }
            restrictPermissions(stateFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static void restrictPermissions(Path file) {
        try {
            Files.setPosixFilePermissions(file, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE));
        } catch (IOException | UnsupportedOperationException ignored) {
            // POSIX permissions are unavailable on some CI and Windows file systems.
        }
    }

    private static boolean isExpired(Path stateFile, EnvironmentConfig config) {
        int maxAgeHours = config.getInt("auth.state.max-age.hours", 24);
        if (maxAgeHours <= 0) {
            return false;
        }
        try {
            Instant savedAt = Files.getLastModifiedTime(stateFile).toInstant();
            return savedAt.plus(Duration.ofHours(maxAgeHours)).isBefore(Instant.now());
        } catch (IOException exception) {
            return true;
        }
    }

    private static Path stateFile(EnvironmentConfig config, Browser browser) {
        String configuredDirectory = config.getString("auth.state.directory", ".browser-state");
        Path directory = Path.of(configuredDirectory);
        if (!directory.isAbsolute()) {
            directory = Path.of(System.getProperty("user.dir")).resolve(directory);
        }
        String profile = config.getString(
                "auth.state.profile",
                config.getString("auth.username", "default"));
        String fileName = config.environment() + "-" + browser.name().toLowerCase()
                + "-" + shortHash(profile) + ".properties";
        return directory.normalize().resolve(fileName);
    }

    private static String shortHash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (int index = 0; index < 6; index++) {
                hash.append(String.format("%02x", digest[index]));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String origin(String url) {
        try {
            URI uri = URI.create(url);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("URL must include scheme and host: " + url);
            }
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), "/", null, null)
                    .toString();
        } catch (IllegalArgumentException | URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid base URL for browser state: " + url, exception);
        }
    }

    private static boolean sameOrigin(String currentUrl, String expectedOrigin) {
        try {
            URI current = URI.create(currentUrl);
            URI expected = URI.create(expectedOrigin);
            return current.getScheme().equalsIgnoreCase(expected.getScheme())
                    && current.getHost().equalsIgnoreCase(expected.getHost())
                    && effectivePort(current) == effectivePort(expected);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return false;
        }
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static Object lockFor(Path stateFile) {
        return FILE_LOCKS.computeIfAbsent(stateFile.toAbsolutePath(), ignored -> new Object());
    }

    private static void put(Properties state, String key, String value) {
        if (value != null) {
            state.setProperty(key, value);
        }
    }

    private static void optional(
            Properties state, String key, java.util.function.Consumer<String> consumer) {
        String value = state.getProperty(key);
        if (value != null && !value.isBlank()) {
            consumer.accept(value);
        }
    }
}
