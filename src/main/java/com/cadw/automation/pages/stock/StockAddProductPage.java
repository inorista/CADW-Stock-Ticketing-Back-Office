package com.cadw.automation.pages.stock;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.cadw.automation.pages.BasePage;

public class StockAddProductPage extends BasePage {
  private static final By LOADING_OVERLAY = By.cssSelector("app-spinner .content");
  private static final By NEW_PRODUCT_FORM = By.cssSelector("app-create-product > div > form");

  private static final By PRODUCT_CODE_INPUT = By.cssSelector("input[formcontrolname='code']");
  private static final By PRODUCT_NAME_INPUT = By.cssSelector("input[formcontrolname='name']");
  private static final By PRODUCT_PRINT_TEXT_INPUT =
      By.cssSelector("input[formcontrolname='printText']");
  private static final By PRODUCT_GROUP_INPUT =
      By.cssSelector("ng-select[formcontrolname='productGroupId']");
  private static final By PRODUCT_TYPE_ITEM =
      By.xpath("//app-create-product/div/form//section/div/div");
  private static final By COMMISSION_INPUT = By.cssSelector("input[formcontrolname='commission']");
  private static final By ORDER_INPUT = By.cssSelector("input[formcontrolname='sortPriority']");
  private static final By ACTIVE_SWITCH =
      By.cssSelector("app-create-product > div > form button[role='switch']");
  private static final String ARIA_ACTIVE_STATUS = "aria-checked";
  private static final By MAXIMUM_DISCOUNT_INPUT =
      By.cssSelector("input[formcontrolname='maximumDiscount']");

  private static final By CHECKBOX_OPTIONS = By.cssSelector("app-create-product mat-checkbox");
  private static final By ALLERGEN_CHECKBOXES =
      By.cssSelector("app-create-product mat-checkbox.allergen-checkbox[title]");
  private static final By OPEN_NG_SELECT_OPTIONS =
      By.cssSelector("ng-dropdown-panel[role='listbox'] div[role='option'].ng-option");

  private static final By PURCHASING_VAT_INPUT =
      By.cssSelector("product-stock-item ng-select[formcontrolname='taxGroupId']");
  private static final By PRICE_TYPE_INPUT =
      By.cssSelector("product-price mat-select[formcontrolname='priceType']");
  private static final By OPEN_MAT_SELECT_OPTIONS =
      By.cssSelector("div[role='listbox'] mat-option[role='option']");
  private static final By PRICE_LEVEL_INPUT =
      By.cssSelector("product-price ng-select[formcontrolname='priceLevel']");
  private static final By TAX_GROUP_INPUT =
      By.cssSelector("product-price ng-select[formcontrolname='taxGroup']");
  private static final By PRICE_INPUT =
      By.cssSelector("product-price input[formcontrolname='priceValue']");
  private static final By COST_INPUT =
      By.cssSelector("product-price input[formcontrolname='cost']");
  private static final By VALID_FROM_DATE =
      By.cssSelector("product-price input[formcontrolname='validFrom']");
  private static final By DATE_PICKER = By.cssSelector("bs-datepicker-container");
  private static final By ENABLED_DATES =
      By.cssSelector(
          "bs-datepicker-container .bs-datepicker-body td:not(.disabled) span:not(.disabled)");
  private static final By ADD_PRICE_BUTTON =
      By.xpath("//product-price//button[normalize-space()='Add']");

  private static final By EXPORT_CODE_INPUT =
      By.cssSelector("product-configuration ng-select:not([formcontrolname])");
  private static final By AUTO_GENERATE_BARCODE_BUTTON =
      By.xpath(
          "//app-create-product//input[@formcontrolname='barcode']"
              + "/ancestor::form[1]//button[normalize-space()='Auto generate']");
  private static final By ADD_BARCODE_BUTTON =
      By.xpath(
          "//app-create-product//input[@formcontrolname='barcode']"
              + "/ancestor::form[1]//button[normalize-space()='Add']");
  private static final By SAVE_PRODUCT_BUTTON =
      By.xpath("//app-create-product//button[normalize-space()='Save']");

  public StockAddProductPage(WebDriver driver) {
    super(driver);
  }

  public boolean isAddProductPageDisplayed() {
    waitUntilVisible(NEW_PRODUCT_FORM);
    waitForLoadingToFinish();
    return isDisplayed(NEW_PRODUCT_FORM);
  }

  public void enterProductCode(String productCode) {
    typeWhenReady(PRODUCT_CODE_INPUT, productCode);
  }

  public void enterProductName(String productName) {
    typeWhenReady(PRODUCT_NAME_INPUT, productName);
  }

  public void enterProductPrintText(String productPrintText) {
    typeWhenReady(PRODUCT_PRINT_TEXT_INPUT, productPrintText);
  }

  public void selectProductGroup(String productGroup) {
    selectNgOption(PRODUCT_GROUP_INPUT, productGroup);
  }

  public void selectProductType(String productType) {
    clickMatchingText(PRODUCT_TYPE_ITEM, productType);
  }

  public void enterCommission(String commission) {
    typeWhenReady(COMMISSION_INPUT, commission);
  }

  public void enterOrder(String order) {
    typeWhenReady(ORDER_INPUT, order);
  }

  public void selectAllergen(String allergen) {
    waitForLoadingToFinish();
    WebElement checkbox =
        waitUntilAllVisible(ALLERGEN_CHECKBOXES).stream()
            .filter(WebElement::isDisplayed)
            .filter(item -> allergen.trim().equalsIgnoreCase(item.getAttribute("title")))
            .findFirst()
            .orElseThrow(
                () ->
                    new NoSuchElementException("Allergen '%s' was not found".formatted(allergen)));
    WebElement input = checkbox.findElement(By.cssSelector("input[type='checkbox']"));
    if (!input.isSelected()) {
      checkbox.click();
    }
  }

  public void selectActive(String active) {
    waitForLoadingToFinish();
    WebElement activeSwitch = waitUntilVisible(ACTIVE_SWITCH);
    boolean expectedActive;
    if ("Yes".equalsIgnoreCase(active)) {
      expectedActive = true;
    } else if ("No".equalsIgnoreCase(active)) {
      expectedActive = false;
    } else {
      throw new IllegalArgumentException("Active must be 'Yes' or 'No': " + active);
    }

    boolean currentlyActive = Boolean.parseBoolean(activeSwitch.getAttribute(ARIA_ACTIVE_STATUS));
    if (currentlyActive != expectedActive) {
      clickWhenReady(ACTIVE_SWITCH);
    }
  }

  public void clickActiveSwitch() {
    clickWhenReady(ACTIVE_SWITCH);
  }

  public void enterMaximumDiscount(String maximumDiscount) {
    typeWhenReady(MAXIMUM_DISCOUNT_INPUT, maximumDiscount);
  }

  public void selectPrintOptions(String printOptions) {
    selectCheckbox(printOptions);
  }

  public void selectPurchasingVAT(String purchasingVAT) {
    selectNgOption(PURCHASING_VAT_INPUT, purchasingVAT);
  }

  public void enterPriceType(String priceType) {
    clickWhenReady(PRICE_TYPE_INPUT);
    clickMatchingText(OPEN_MAT_SELECT_OPTIONS, priceType);
  }

  public void enterPriceLevel(String priceLevel) {
    selectNgOption(PRICE_LEVEL_INPUT, priceLevel);
  }

  public void enterTaxGroup(String taxGroup) {
    selectNgOption(TAX_GROUP_INPUT, taxGroup);
  }

  public void enterPrice(String price) {
    typeWhenReady(PRICE_INPUT, price);
  }

  public void enterCost(String cost) {
    typeWhenReady(COST_INPUT, cost);
  }

  public void selectValidFromDate() {
    clickWhenReady(VALID_FROM_DATE);
    waitUntilVisible(DATE_PICKER);
    List<WebElement> enabledDates = waitUntilAllVisible(ENABLED_DATES);
    if (enabledDates.isEmpty()) {
      throw new NoSuchElementException("No enabled date is available");
    }
    enabledDates.get(0).click();
  }

  public void clickAddPrice() {
    clickWhenReady(ADD_PRICE_BUTTON);
  }

  public void userSearchOrSelectAnExportCode(String exportCode) {
    selectNgOption(EXPORT_CODE_INPUT, exportCode);
  }

  public void clickAutoGenerateBarcode() {
    clickWhenReady(AUTO_GENERATE_BARCODE_BUTTON);
  }

  public void clickAddBarcode() {
    clickWhenReady(ADD_BARCODE_BUTTON);
  }

  public void clickSave() {
    clickWhenReady(SAVE_PRODUCT_BUTTON);
  }

  public StockProductsPage saveAndWaitForProducts() {
    clickSave();
    waitUntilUrlDoesNotContain("/stock/products/create");
    waitForLoadingToFinish();
    return new StockProductsPage(driver);
  }

  private void selectNgOption(By selectLocator, String optionText) {
    clickWhenReady(selectLocator);
    clickMatchingText(OPEN_NG_SELECT_OPTIONS, optionText);
  }

  private void selectCheckbox(String optionText) {
    waitForLoadingToFinish();
    WebElement checkbox = findMatchingText(CHECKBOX_OPTIONS, optionText);
    WebElement input = checkbox.findElement(By.cssSelector("input[type='checkbox']"));
    if (!input.isSelected()) {
      checkbox.click();
    }
  }

  private void clickMatchingText(By itemsLocator, String expectedText) {
    waitForLoadingToFinish();
    WebElement item = findMatchingText(itemsLocator, expectedText);
    try {
      item.click();
    } catch (ElementClickInterceptedException exception) {
      waitForLoadingToFinish();
      item = findMatchingText(itemsLocator, expectedText);
      item.click();
    }
  }

  private WebElement findMatchingText(By itemsLocator, String expectedText) {
    List<WebElement> items = waitUntilAllVisible(itemsLocator);
    return items.stream()
        .filter(WebElement::isDisplayed)
        .filter(item -> item.getText().trim().equalsIgnoreCase(expectedText.trim()))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    "Option '%s' was not found using %s".formatted(expectedText, itemsLocator)));
  }

  private void typeWhenReady(By locator, String value) {
    waitForLoadingToFinish();
    scrollIntoView(locator);
    type(locator, value);
  }

  private void clickWhenReady(By locator) {
    waitForLoadingToFinish();
    scrollIntoView(locator);
    try {
      click(locator);
    } catch (ElementClickInterceptedException exception) {
      waitForLoadingToFinish();
      scrollIntoView(locator);
      click(locator);
    }
  }

  private void waitForLoadingToFinish() {
    waitUntilInvisible(LOADING_OVERLAY);
  }
}
