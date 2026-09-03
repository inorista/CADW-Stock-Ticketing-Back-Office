package com.cadw.automation.support;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.pages.home.HomePage;
import com.cadw.automation.pages.login.LoginPage;
import java.net.URI;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class AuthenticationSupport {
    private static final By LOGIN_FORM = By.cssSelector("app-login form");
    private static final By AUTHENTICATED_LAYOUT = By.cssSelector("app-layout");

    private AuthenticationSupport() {
    }

    public static void ensureAuthenticated() {
        EnvironmentConfig config = EnvironmentConfig.active();
        WebDriver driver = DriverSession.driver();
        if (!hasSameOrigin(driver.getCurrentUrl(), config.baseUrl())) {
            driver.get(config.baseUrl());
        }

        try {
            new WebDriverWait(driver, Duration.ofSeconds(config.explicitWaitSeconds()))
                    .until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(AUTHENTICATED_LAYOUT),
                            ExpectedConditions.presenceOfElementLocated(LOGIN_FORM)));
        } catch (TimeoutException exception) {
            throw new TimeoutException(
                    "Application did not render the authenticated layout or login form. "
                            + "Expected base URL: " + config.baseUrl()
                            + ", current URL: " + safeCurrentUrl(driver)
                            + ", page title: " + safeTitle(driver),
                    exception);
        }
        if (!driver.findElements(AUTHENTICATED_LAYOUT).isEmpty()) {
            return;
        }

        LoginPage loginPage = new LoginPage(driver)
                .enterEmail(config.getString("auth.username"))
                .enterPassword(config.getString("auth.password"));
        HomePage homePage = loginPage.clickLoginButton();
        if (!homePage.isDashboardDisplayed()) {
            throw new IllegalStateException(
                    "Configured credentials did not produce an authenticated session");
        }
        DriverFactory.saveBrowserState();
    }

    public static void resetToLoggedOutState() {
        DriverFactory.resetCurrentBrowserSession();
    }

    private static boolean hasSameOrigin(String currentUrl, String baseUrl) {
        try {
            URI current = URI.create(currentUrl);
            URI expected = URI.create(baseUrl);
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

    private static String safeCurrentUrl(WebDriver driver) {
        try {
            return driver.getCurrentUrl();
        } catch (RuntimeException exception) {
            return "<unavailable>";
        }
    }

    private static String safeTitle(WebDriver driver) {
        try {
            return driver.getTitle();
        } catch (RuntimeException exception) {
            return "<unavailable>";
        }
    }
}
