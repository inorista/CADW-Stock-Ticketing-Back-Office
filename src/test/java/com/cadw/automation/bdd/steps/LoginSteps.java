package com.cadw.automation.bdd.steps;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.pages.home.HomePage;
import com.cadw.automation.pages.login.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.testng.Assert;

public class LoginSteps {
    private LoginPage loginPage;
    private HomePage homePage;

    @Given("User open the Aramark login page")
    public void openLoginPage() {
        loginPage = new LoginPage(DriverSession.driver()).open();
    }

    @Then("User is on login page")
    public void verifyLoginPage() {
        Assert.assertTrue(loginPage.isEmailDisplayed(), "Email field is not displayed");
        Assert.assertTrue(loginPage.isPasswordDisplayed(), "Password field is not displayed");
        Assert.assertTrue(loginPage.isSignInEnabled(), "Sign-in button is not enabled");
    }

    @Then("User enters the configured valid credentials")
    public void enterConfiguredCredentials() {
        EnvironmentConfig config = EnvironmentConfig.active();
        loginPage.enterEmail(config.getString("auth.username"));
        loginPage.enterPassword(config.getString("auth.password"));
    }

    @Then("User enters invalid {string} and {string}")
    public void enterInvalidCredentials(String username, String password) {
        loginPage.enterEmail(username);
        loginPage.enterPassword(password);
    }

    @Then("User clicks login button")
    public void clickLoginButton() {
        homePage = loginPage.clickLoginButton();
    }

    @Then("User is navigated to home page")
    public void verifyHomePage() {
        Assert.assertTrue(homePage.isDashboardDisplayed(), "Home dashboard is not displayed");
        DriverFactory.saveBrowserState();
    }

    @Then("Browser show invalid username and password")
    public void verifyInvalidCredentialsMessage() {
        Assert.assertTrue(loginPage.isErrorMessageDialogShowed(), "Login error is not displayed");
    }
}
