package com.cadw.automation.pages.stock;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.cadw.automation.pages.BasePage;

public class StockOrderPage extends BasePage {

  public StockOrderPage(WebDriver driver) {
    super(driver);
  }

  private static final By ADD_ORDER_BUTTON = By.cssSelector("app-order table thead button");
  private static final By ORDER_TABLE_SECTION = By.cssSelector("app-order table");

  public boolean isCurrentURLIsOrderPage() {
    return currentUrl().contains("/stock/orders");
  }

  public boolean isOrderPageDisplayed() {
    waitUntilVisible(ORDER_TABLE_SECTION);
    return isDisplayed(ORDER_TABLE_SECTION);
  }

  public void clickAddOrderButton() {
    waitUntilVisible(ADD_ORDER_BUTTON);
    clickByJs(ADD_ORDER_BUTTON);
  }
}
