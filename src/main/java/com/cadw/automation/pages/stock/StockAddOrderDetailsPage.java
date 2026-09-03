package com.cadw.automation.pages.stock;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.cadw.automation.pages.BasePage;

public class StockAddOrderDetailsPage extends BasePage {
  public StockAddOrderDetailsPage(WebDriver driver) {
    super(driver);
  }

  private static final By PURCHASE_ORDER = By.cssSelector("purchase-child-order mat-tab-group");
  private static final By SAVE_BUTTON =
      By.xpath(
          "//order-details//button[normalize-space()='Save' or .//*[normalize-space()='Save']]");
  private static final String path = "stock/orders/add";
  private static final By LOADING_OVERLAY = By.cssSelector("app-spinner .content");

  public boolean verifyIfCurrentPageIsStockAddDetailsPage() {
    return isPurchaseOrderDisplayed() && isCurrentURLStockOrderAddDetail();
  }

  private void waitForLoadingToFinish() {
    waitUntilInvisible(LOADING_OVERLAY);
  }

  public boolean isPurchaseOrderDisplayed() {
    waitForLoadingToFinish();
    waitUntilVisible(PURCHASE_ORDER);
    return isDisplayed(PURCHASE_ORDER);
  }

  public boolean isCurrentURLStockOrderAddDetail() {
    String actualCurrentUrl = currentUrl();
    return actualCurrentUrl.contains(path);
  }

  public void clickSave() {
    scrollIntoView(SAVE_BUTTON);
    waitUntilClickable(SAVE_BUTTON);
    clickByJs(SAVE_BUTTON);
  }
}
