package com.cadw.automation.bdd.hooks;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.pages.home.HomePage;
import com.cadw.automation.pages.login.LoginPage;
import com.cadw.automation.utils.ScreenshotUtils;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.Reporter;

public final class CucumberHooks {
    private static final By LOGIN_FORM = By.cssSelector("app-login form");
    private static final By AUTHENTICATED_LAYOUT = By.cssSelector("app-layout");

    @Before(order = 0)
    public void startBrowser(Scenario scenario) {
        DriverFactory.start(testParameter("browser"), testParameter("execution"), scenario.getName());
        if (scenario.getSourceTagNames().contains("@login")) {
            resetToLoggedOutState();
        }
    }

    @Before(value = "@authenticated", order = 1)
    public void ensureAuthenticated() {
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
                .enterEmail(config.getString("username"))
                .enterPassword(config.getString("password"));
        HomePage homePage = loginPage.clickLoginButton();
        if (!homePage.isDashboardDisplayed()) {
            throw new IllegalStateException("Configured credentials did not produce an authenticated session");
        }
        DriverFactory.saveBrowserState();
    }

    @After
    public void stopBrowser(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                ScreenshotUtils.capture(DriverSession.driverOrNull())
                        .ifPresent(bytes -> scenario.attach(bytes, "image/png", "Failure screenshot"));
            }
            DriverFactory.markRemoteStatus(!scenario.isFailed(), scenario.getName());
        } finally {
            DriverFactory.stop(scenario.getSourceTagNames().contains("@authenticated"));
        }
    }

    private static String testParameter(String name) {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null || result.getTestContext() == null
                || result.getTestContext().getCurrentXmlTest() == null) {
            return "";
        }
        String value = result.getTestContext().getCurrentXmlTest().getParameter(name);
        return value == null ? "" : value;
    }

    private static void resetToLoggedOutState() {
        WebDriver driver = DriverSession.driver();
        driver.get(EnvironmentConfig.active().baseUrl());
        driver.manage().deleteAllCookies();
        if (driver instanceof JavascriptExecutor executor) {
            executor.executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        }
        driver.get(EnvironmentConfig.active().baseUrl());
    }
}
