package com.cadw.automation.state;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.Browser;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BrowserStateManagerTest {

    @Test
    public void savesAndRestoresCookiesAndWebStorage() throws Exception {
        Path stateDirectory = Files.createTempDirectory("browser-state-test-");
        System.setProperty("auth.state.directory", stateDirectory.toString());
        try {
            EnvironmentConfig config = EnvironmentConfig.load("dev");
            FakeBrowser source = new FakeBrowser(config.baseUrl());
            source.cookies.add(new Cookie.Builder("access_token", "cookie-token")
                    .path("/")
                    .expiresOn(java.util.Date.from(Instant.now().plusSeconds(3600)))
                    .isHttpOnly(true)
                    .build());
            source.localStorage.put("accessToken", "local-token");
            source.sessionStorage.put("csrf", "session-token");

            Assert.assertTrue(BrowserStateManager.save(source.driver, config, Browser.CHROME));

            FakeBrowser restored = new FakeBrowser("about:blank");
            Assert.assertTrue(BrowserStateManager.restore(restored.driver, config, Browser.CHROME));
            Assert.assertEquals(restored.cookies.stream()
                    .filter(cookie -> "access_token".equals(cookie.getName()))
                    .findFirst()
                    .orElseThrow()
                    .getValue(), "cookie-token");
            Assert.assertEquals(restored.localStorage.get("accessToken"), "local-token");
            Assert.assertEquals(restored.sessionStorage.get("csrf"), "session-token");
            Assert.assertEquals(restored.refreshCount, 1);
        } finally {
            System.clearProperty("auth.state.directory");
            try (var files = Files.list(stateDirectory)) {
                files.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                        // Best-effort cleanup for a temporary test directory.
                    }
                });
            }
            Files.deleteIfExists(stateDirectory);
        }
    }

    private static final class FakeBrowser {
        private final Set<Cookie> cookies = new HashSet<>();
        private final Map<String, String> localStorage = new HashMap<>();
        private final Map<String, String> sessionStorage = new HashMap<>();
        private final WebDriver driver;
        private String currentUrl;
        private int refreshCount;

        private FakeBrowser(String initialUrl) {
            currentUrl = initialUrl;
            WebDriver.Options options = proxy(WebDriver.Options.class, (method, args) -> switch (method.getName()) {
                case "getCookies" -> new HashSet<>(cookies);
                case "addCookie" -> cookies.add((Cookie) args[0]);
                case "deleteAllCookies" -> {
                    cookies.clear();
                    yield null;
                }
                default -> null;
            });
            WebDriver.Navigation navigation = proxy(
                    WebDriver.Navigation.class,
                    (method, args) -> {
                        if ("refresh".equals(method.getName())) {
                            refreshCount++;
                        }
                        return null;
                    });

            driver = (WebDriver) Proxy.newProxyInstance(
                    BrowserStateManagerTest.class.getClassLoader(),
                    new Class<?>[] { WebDriver.class, JavascriptExecutor.class },
                    (proxy, method, args) -> switch (method.getName()) {
                        case "get" -> {
                            currentUrl = (String) args[0];
                            yield null;
                        }
                        case "getCurrentUrl" -> currentUrl;
                        case "manage" -> options;
                        case "navigate" -> navigation;
                        case "executeScript" -> executeScript((String) args[0], (Object[]) args[1]);
                        case "toString" -> "FakeBrowser";
                        default -> null;
                    });
        }

        private Object executeScript(String script, Object[] arguments) {
            if (script.contains("window.localStorage.length")) {
                return new HashMap<>(localStorage);
            }
            if (script.contains("window.sessionStorage.length")) {
                return new HashMap<>(sessionStorage);
            }
            if (script.contains("const storage = window.localStorage")) {
                replaceStorage(localStorage, arguments);
            }
            if (script.contains("const storage = window.sessionStorage")) {
                replaceStorage(sessionStorage, arguments);
            }
            return null;
        }

        private static void replaceStorage(Map<String, String> target, Object[] arguments) {
            target.clear();
            @SuppressWarnings("unchecked")
            Map<String, String> values = (Map<String, String>) arguments[0];
            target.putAll(values);
        }

        @SuppressWarnings("unchecked")
        private static <T> T proxy(Class<T> type, MethodHandler handler) {
            return (T) Proxy.newProxyInstance(
                    BrowserStateManagerTest.class.getClassLoader(),
                    new Class<?>[] { type },
                    (proxy, method, args) -> handler.invoke(method, args));
        }
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
