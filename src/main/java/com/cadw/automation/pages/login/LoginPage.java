package com.cadw.automation.pages.login;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.cadw.automation.pages.BasePage;
import com.cadw.automation.pages.home.HomePage;

public class LoginPage extends BasePage {
  private static final By LOGIN_FORM = By.cssSelector("app-login form");
  private static final By HEADING = By.cssSelector(".ecr-welcome");
  private static final By EMAIL_INPUT = By.cssSelector("app-login input[type='email']");

  private static final By PASSWORD_INPUT = By.cssSelector("app-login input[type='password']");

  private static final By SIGN_IN_BUTTON = By.cssSelector("app-login button[type='submit']");
  private static final By FORGOT_PASSWORD = By.cssSelector(".forgot-password");
  private static final By LOGO = By.cssSelector("app-auth .ecr-logo");
  private static final By ERROR_MESSAGE_DIALOG = By.cssSelector(".message");

  public LoginPage(WebDriver driver) {
    super(driver);
  }

  public LoginPage open() {
    openBaseUrl();
    waitUntilVisible(LOGIN_FORM);
    return this;
  }

  public String getCurrentUrl() {
    return currentUrl();
  }

  public String heading() {
    return textOf(HEADING);
  }

  public boolean isEmailDisplayed() {
    waitUntilVisible(EMAIL_INPUT);
    return isDisplayed(EMAIL_INPUT);
  }

  public boolean isErrorMessageDialogShowed() {
    visible(ERROR_MESSAGE_DIALOG);
    return isDisplayed(ERROR_MESSAGE_DIALOG);
  }

  public boolean isPasswordDisplayed() {
    waitUntilVisible(PASSWORD_INPUT);
    return isDisplayed(PASSWORD_INPUT);
  }

  public boolean isForgotPasswordDisplayed() {
    waitUntilVisible(FORGOT_PASSWORD);
    return isDisplayed(FORGOT_PASSWORD);
  }

  public boolean isLogoDisplayed() {
    waitUntilVisible(LOGO);
    return isDisplayed(LOGO);
  }

  public boolean isSignInEnabled() {
    return isEnabled(SIGN_IN_BUTTON);
  }

  public LoginPage enterEmail(String email) {
    type(EMAIL_INPUT, email);
    return this;
  }

  public LoginPage enterPassword(String password) {
    type(PASSWORD_INPUT, password);
    return this;
  }

  public HomePage clickLoginButton() {
    click(SIGN_IN_BUTTON);
    return new HomePage(driver);
  }

  public LoginPage signIn(String email, String password) {
    enterEmail(email);
    enterPassword(password);
    click(SIGN_IN_BUTTON);
    return this;
  }
}
