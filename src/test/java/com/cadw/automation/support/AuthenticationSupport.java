package com.cadw.automation.support;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.pages.home.HomePage;
import com.cadw.automation.pages.login.LoginPage;
import java.time.Duration;
import org.openqa.selenium.By;
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
        if (driver.getCurrentUrl() == null || "about:blank".equals(driver.getCurrentUrl())) {
            driver.get(config.baseUrl());
        }

        new WebDriverWait(driver, Duration.ofSeconds(config.explicitWaitSeconds()))
                .until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(AUTHENTICATED_LAYOUT),
                        ExpectedConditions.presenceOfElementLocated(LOGIN_FORM)));
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
}
