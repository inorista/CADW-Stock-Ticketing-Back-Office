package com.cadw.automation.tests;

import com.cadw.automation.base.BaseTest;
import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.pages.home.HomePage;
import com.cadw.automation.pages.login.LoginPage;
import com.cadw.automation.support.AuthenticationSupport;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void openLoggedOutSession() {
        AuthenticationSupport.resetToLoggedOutState();
        loginPage = new LoginPage(driver()).open();
    }

    @Test(groups = { "testng", "login", "smoke" })
    public void loginWithConfiguredCredentials() {
        EnvironmentConfig config = EnvironmentConfig.active();

        Assert.assertTrue(loginPage.isEmailDisplayed(), "Email field is not displayed");
        Assert.assertTrue(loginPage.isPasswordDisplayed(), "Password field is not displayed");
        HomePage homePage = loginPage
                .enterEmail(config.getString("auth.username"))
                .enterPassword(config.getString("auth.password"))
                .clickLoginButton();

        Assert.assertTrue(homePage.isDashboardDisplayed(), "Home dashboard is not displayed");
        DriverFactory.saveBrowserState();
    }

    @Test(groups = { "testng", "login", "smoke" })
    public void rejectInvalidCredentials() {
        loginPage
                .enterEmail("invalid-user@example.invalid")
                .enterPassword("invalid-password")
                .clickLoginButton();

        Assert.assertTrue(loginPage.isErrorMessageDialogShowed(),
                "Invalid-credentials message is not displayed");
    }
}
