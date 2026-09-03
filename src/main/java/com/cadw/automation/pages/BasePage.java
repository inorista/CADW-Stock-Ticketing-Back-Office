package com.cadw.automation.pages;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cadw.automation.config.EnvironmentConfig;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final EnvironmentConfig config;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.config = EnvironmentConfig.active();
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(config.explicitWaitSeconds()));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void clear(By locator) {
        visible(locator).clear();
    }

    protected List<WebElement> waitUntilElementsAreVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected boolean isElementDisplayed(By locator) {
        return isDisplayed(locator);
    }

    protected String getText(By locator) {
        return textOf(locator);
    }

    protected void sendKeys(By locator, CharSequence keysToSend) {
        visible(locator).sendKeys(keysToSend);
    }

    protected void clickByJsExecutor(By locator) {
        clickByJs(locator);
    }

    protected WebElement waitUntilVisible(By locator) {
        return visible(locator);
    }

    protected void waitUntilInVisible(By locator) {
        waitUntilInvisible(locator);
    }

    protected boolean waitUntilInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected WebElement waitUntilPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement waitUntilClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> waitUntilAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected <T> T waitUntilCondition(Function<? super WebDriver, T> condition) {
        return wait.until(condition);
    }

    protected boolean waitUntilUrlDoesNotContain(String value) {
        return wait.until(ExpectedConditions.not(ExpectedConditions.urlContains(value)));
    }

    protected void openBaseUrl() {
        driver.get(config.baseUrl());
    }

    protected void navigateTo(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            throw new IllegalArgumentException("Navigation target must not be blank");
        }
        if (pathOrUrl.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            driver.get(pathOrUrl);
            return;
        }
        String baseUrl = config.baseUrl().replaceFirst("/+$", "");
        driver.get(baseUrl + "/" + pathOrUrl.replaceFirst("^/+", ""));
    }

    protected String currentUrl() {
        return driver.getCurrentUrl();
    }

    protected String textOf(By locator) {
        return visible(locator).getText();
    }

    protected String attributeOf(By locator, String attribute) {
        return visible(locator).getAttribute(attribute);
    }

    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
        } catch (NoSuchElementException | StaleElementReferenceException exception) {
            return false;
        }
    }

    protected boolean isEnabled(By locator) {
        return visible(locator).isEnabled();
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected void sendEnterKey(By locator) {
        visible(locator).sendKeys(Keys.ENTER);
    }

    protected void scrollIntoView(By locator) {
        scrollIntoView(waitUntilPresent(locator));
    }

    protected void scrollIntoView(WebElement element) {
        javascript().executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                element);
    }

    protected void clickByJs(By locator) {
        clickByJs(waitUntilClickable(locator));
    }

    protected void clickByJs(WebElement element) {
        javascript().executeScript("arguments[0].click();", element);
    }

    private JavascriptExecutor javascript() {
        if (!(driver instanceof JavascriptExecutor executor)) {
            throw new IllegalStateException("WebDriver does not support JavaScript execution");
        }
        return executor;
    }
}
