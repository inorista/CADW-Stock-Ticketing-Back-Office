package com.cadw.automation.pages.stock;

import java.math.BigDecimal;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.cadw.automation.pages.BasePage;

public class StockUpdateProductPage extends BasePage {
  private static final By LOADING_OVERLAY = By.cssSelector("app-spinner .content");
  private static final By UPDATE_PRODUCT = By.cssSelector("update-product");
  private static final By NAME_INPUT =
      By.cssSelector("update-product input[formcontrolname='name']");
  private static final By PRICE_TAB =
      By.xpath("//update-product//*[@role='tab' and normalize-space()='Price']");
  private static final By ACTIVE_PRICE_TAB =
      By.cssSelector("mat-tab-body.mat-mdc-tab-body-active price-tab");
  private static final By FIRST_PRICE_EDIT =
      By.cssSelector("price-table-template tbody tr:first-child .action-cell .edit");
  private static final By PRICE_INPUT =
      By.cssSelector("price-template input[formcontrolname='priceValue']");
  private static final By ADD_PRICE_BUTTON =
      By.xpath("//price-template//button[normalize-space()='Add']");
  private static final By FIRST_PRICE_VALUE =
      By.cssSelector("price-table-template tbody tr:first-child td.cdk-column-price");
  private static final By UPDATE_BUTTON =
      By.xpath("//price-tab//button[normalize-space()='Update']");
  private static final By UPDATE_PRODUCT_DETAILS_BUTTON =
      By.xpath("//prod-tab//button[normalize-space()='Update']");

  public StockUpdateProductPage(WebDriver driver) {
    super(driver);
  }

  public StockUpdateProductPage waitUntilLoaded() {
    waitUntilVisible(UPDATE_PRODUCT);
    waitForLoadingToFinish();
    waitUntilCondition(
        webDriver -> {
          String name = webDriver.findElement(NAME_INPUT).getAttribute("value");
          return name != null && !name.isBlank();
        });
    return this;
  }

  public void enterProductName(String productName) {
    waitForLoadingToFinish();
    scrollIntoView(NAME_INPUT);
    type(NAME_INPUT, productName);
  }

  public String getProductName() {
    return attributeOf(NAME_INPUT, "value").trim();
  }

  public void updateFirstPrice(String newPrice) {
    openPriceTab();
    clickWhenReady(FIRST_PRICE_EDIT);
    waitUntilCondition(
        webDriver -> {
          String price = webDriver.findElement(PRICE_INPUT).getAttribute("value");
          return price != null && !price.isBlank();
        });
    type(PRICE_INPUT, newPrice);
    clickWhenReady(ADD_PRICE_BUTTON);
    waitForLoadingToFinish();
    waitUntilCondition(
        webDriver -> priceEquals(webDriver.findElement(FIRST_PRICE_VALUE).getText(), newPrice));
  }

  public boolean isFirstPrice(String expectedPrice) {
    openPriceTab();
    return priceEquals(textOf(FIRST_PRICE_VALUE), expectedPrice);
  }

  public StockProductsPage saveProductDetails() {
    clickWhenReady(UPDATE_PRODUCT_DETAILS_BUTTON);
    waitUntilUrlDoesNotContain("/stock/products/update/");
    waitForLoadingToFinish();
    return new StockProductsPage(driver);
  }

  public StockProductsPage savePriceChanges() {
    clickWhenReady(UPDATE_BUTTON);
    waitUntilUrlDoesNotContain("/stock/products/update/");
    waitForLoadingToFinish();
    return new StockProductsPage(driver);
  }

  private void openPriceTab() {
    waitForLoadingToFinish();
    if (isDisplayed(ACTIVE_PRICE_TAB)) {
      return;
    }
    clickWhenReady(PRICE_TAB);
    waitUntilVisible(ACTIVE_PRICE_TAB);
    waitForLoadingToFinish();
  }

  private void clickWhenReady(By locator) {
    waitForLoadingToFinish();
    WebElement element = waitUntilPresent(locator);
    scrollIntoView(element);
    try {
      waitUntilClickable(locator).click();
    } catch (ElementClickInterceptedException exception) {
      waitForLoadingToFinish();
      clickByJs(locator);
    }
  }

  private void waitForLoadingToFinish() {
    waitUntilInvisible(LOADING_OVERLAY);
  }

  private static boolean priceEquals(String actualPrice, String expectedPrice) {
    try {
      return new BigDecimal(actualPrice.trim()).compareTo(new BigDecimal(expectedPrice.trim()))
          == 0;
    } catch (NumberFormatException exception) {
      return false;
    }
  }
}
