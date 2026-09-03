package com.cadw.automation.pages.stock;

import com.cadw.automation.pages.BasePage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class StockAddOrderPage extends BasePage {
  private static final By COMMENTS_TEXT_INPUT = By.cssSelector("location-order textarea");
  private static final By SEARCH_FOR_SUPPLIER_INPUT = By.cssSelector("supplier-order input");
  private static final By SUPPLIERS_TABLE_SECTION = By.cssSelector("supplier-order table");
  private static final By SUPPLIER_ITEM = By.cssSelector("supplier-order table tbody tr");
  private static final By SUPPLIER_SELECT_CHECKBOX =
      By.cssSelector("td:nth-child(5) input[type='checkbox']");
  private static final By SUPPLIER_NAME = By.cssSelector("td:nth-child(2)");
  private static final By NEXT_BUTTON =
      By.xpath("//create-order//button[normalize-space()='Next' or .//*[normalize-space()='Next']]");
  private static final By LOADING_OVERLAY = By.cssSelector("app-spinner .content");

  public StockAddOrderPage(WebDriver driver) {
    super(driver);
  }

  public boolean isStockSupplierTableDisplayed() {
    waitUntilVisible(SUPPLIERS_TABLE_SECTION);
    return isDisplayed(SUPPLIERS_TABLE_SECTION);
  }

  public boolean verifyIfCurrentPageIsStockAddOrderPage() {
    return isStockSupplierTableDisplayed() && isCurrentURLAddOrder();
  }

  public boolean isCurrentURLAddOrder() {
    return currentUrl().equals(config.baseUrl() + "stock/orders/add");
  }

  public void enterComments(String comments) {
    type(COMMENTS_TEXT_INPUT, comments);
  }

  public void enterSearchForSupplier(String search) {
    type(SEARCH_FOR_SUPPLIER_INPUT, search);
    sendEnterKey(SEARCH_FOR_SUPPLIER_INPUT);
  }

  public void selectSupplierByName(String supplierName) {
    waitForLoadingToFinish();
    List<WebElement> supplierItems = waitUntilAllVisible(SUPPLIER_ITEM);
    WebElement supplier = supplierItems.stream()
        .filter(WebElement::isDisplayed)
        .filter(item -> item.findElement(SUPPLIER_NAME).getText().trim()
            .equalsIgnoreCase(supplierName.trim()))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException(
            "Supplier was not found: " + supplierName));
    WebElement supplierCheckbox = supplier.findElement(SUPPLIER_SELECT_CHECKBOX);
    scrollIntoView(supplierCheckbox);
    clickByJs(supplierCheckbox);
  }

  public StockAddOrderDetailsPage clickNext() {
    scrollIntoView(NEXT_BUTTON);
    waitUntilClickable(NEXT_BUTTON);
    clickByJs(NEXT_BUTTON);
    waitForLoadingToFinish();
    return new StockAddOrderDetailsPage(driver);
  }

  private void waitForLoadingToFinish() {
    waitUntilInvisible(LOADING_OVERLAY);
  }
}
