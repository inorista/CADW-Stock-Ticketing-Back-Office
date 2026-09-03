package com.cadw.automation.pages.stock;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.cadw.automation.pages.BasePage;

public class StockProductsPage extends BasePage {

  public StockProductsPage(WebDriver driver) {
    super(driver);
  }

  private static final String PATH = "/stock/products";
  private static final By LOADING_OVERLAY = By.cssSelector("app-spinner .content");
  private static final By PRODUCT_SECTION = By.cssSelector("app-stock app-product");
  private static final By PRODUCT_ITEM = By.cssSelector("app-product tbody tr");
  private static final By SHOPIFY_SYNC_BUTTON =
      By.xpath("//button[normalize-space()='Shopify sync']");
  private static final By BULK_DOWNLOAD_UPDATE_BUTTON =
      By.cssSelector(
          "button[class='btn mdc-button mat-mdc-button mat-unthemed mat-mdc-button-base cdk-focused cdk-mouse-focused'] span[class='mat-mdc-button-touch-target']");
  private static final By SEARCH_PRODUCT_INPUT =
      By.cssSelector("input[placeholder='Search for a product...']");
  private static final By SHOPIFY_SUCCESS_TOAST =
      By.cssSelector("mat-snack-bar-container.success .toast.show");
  private static final String TICKETING_SYNCHRONISE_SUCCESSFULLY =
      "Ticketing: Successfully partially synchronized";
  private static final String STOCK_SYNCHRONISE_SUCCESSFULLY =
      "Stock: Successfully partially synchronized";
  private static final By TICKETING_SYNCHRONIZED_MESSAGE =
      syncToastTitle(TICKETING_SYNCHRONISE_SUCCESSFULLY);
  private static final By STOCK_SYNCHRONIZED_MESSAGE =
      syncToastTitle(STOCK_SYNCHRONISE_SUCCESSFULLY);
  private static final By SYNCHRONISE_BUTTON =
      By.xpath("//img[contains(@src,'sync-icon-1.png')]/parent::div[contains(@class,'img')]");
  // input[@id='mat-input-10']
  private static final By CODE_SEARCH_INPUT =
      By.xpath("(//app-product//input[@matinput and @placeholder='Search..'])[1]");
  private static final By PRODUCT_SEARCH_INPUT =
      By.xpath("(//app-product//input[@matinput and @placeholder='Search..'])[2]");
  private static final By ACTIVE_SWITCH =
      By.cssSelector("button[id^='mat-mdc-slide-toggle-'][id$='-button']");
  private static final By ADD_PRODUCT_BUTTON = By.cssSelector("button.addBtn");
  private static final By STOCK_MENU_DROPDOWN =
      By.xpath("//app-menu//*[self::div or self::button][normalize-space()='Stock']");
  private static final By STOCK_MENU = By.cssSelector("app-menu");
  private static final By ORDERS_ITEM =
      By.xpath("//app-menu//a[.//*[normalize-space()='Orders'] or normalize-space()='Orders']");
  private static final By NO_DATA_MESSAGE = By.xpath("//strong[@class='no-data-text']");
  private static final By DELETE_CONFIRMATION_BUTTON =
      By.xpath(
          "//button[translate(normalize-space(.),"
              + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='yes'"
              + " or translate(normalize-space(.),"
              + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='confirm'"
              + " or translate(normalize-space(.),"
              + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='delete']");

  public StockAddProductPage clickAddButton() {
    waitForLoadingToFinish();
    visible(ADD_PRODUCT_BUTTON);
    clickByJs(ADD_PRODUCT_BUTTON);
    return new StockAddProductPage(driver);
  }

  public StockProductsPage open() {
    navigateTo(PATH);
    waitForLoadingToFinish();
    return this;
  }

  public StockProductsPage openStockLanding() {
    navigateTo("/stock/");
    waitForLoadingToFinish();
    return this;
  }

  public boolean isStockLandingAvailable() {
    return waitUntilCondition(
        webDriver -> {
          String currentUrl = webDriver.getCurrentUrl();
          String bodyText = webDriver.findElement(By.tagName("body")).getText().trim();
          boolean stockRoute = currentUrl.contains("/stock/") || currentUrl.endsWith("/stock");
          boolean loading = webDriver.findElements(LOADING_OVERLAY).stream()
              .anyMatch(WebElement::isDisplayed);
          return stockRoute && !loading && !bodyText.isBlank();
        });
  }

  public boolean isProductsPageAvailable() {
    waitForLoadingToFinish();
    waitUntilVisible(PRODUCT_SECTION);
    waitUntilVisible(ADD_PRODUCT_BUTTON);
    return currentUrl().contains(PATH)
        && isDisplayed(PRODUCT_SECTION)
        && isDisplayed(ADD_PRODUCT_BUTTON);
  }

  public void enterProductSearch(String keyword) {
    type(PRODUCT_SEARCH_INPUT, keyword);
  }

  public void enterCodeSearch(String keyword) {
    type(CODE_SEARCH_INPUT, keyword);
  }

  public void enterKeywordToProductNameSearchBar(String keyword) {
    waitUntilVisible(PRODUCT_SEARCH_INPUT);
    clickByJs(PRODUCT_SEARCH_INPUT);
    type(PRODUCT_SEARCH_INPUT, keyword);
  }

  public void enterKeywordToProductCodeSearchBar(String keyword) {
    waitUntilVisible(CODE_SEARCH_INPUT);
    clickByJs(CODE_SEARCH_INPUT);
    type(CODE_SEARCH_INPUT, keyword);
  }

  public void sendEnterKey() {
    sendEnterKey(PRODUCT_SEARCH_INPUT);
  }

  public boolean validateNoDataMessage() {
    waitUntilVisible(NO_DATA_MESSAGE);
    return isDisplayed(NO_DATA_MESSAGE);
  }

  public boolean validateProductList(String productName, String productCode) {
    List<WebElement> productList = waitUntilAllVisible(PRODUCT_ITEM);
    if (productList.isEmpty()) {
      return false;
    }
    return productList.stream()
        .anyMatch(
            product -> {
              String actualProductName = product.findElement(By.xpath(".//td[3]")).getText();
              String actualProductCode = product.findElement(By.xpath(".//td[1]")).getText();
              return actualProductName.toLowerCase().contains(productName.trim().toLowerCase())
                  && actualProductCode.toLowerCase().contains(productCode.trim().toLowerCase());
            });
  }

  public void searchProduct(String productCode, String productName) {
    waitForLoadingToFinish();
    type(CODE_SEARCH_INPUT, productCode);
    type(PRODUCT_SEARCH_INPUT, productName);
    sendEnterKey(PRODUCT_SEARCH_INPUT);
    waitForLoadingToFinish();
    waitUntilCondition(
        webDriver ->
            (productCode.isBlank()
                    ? webDriver.findElements(PRODUCT_ITEM).stream()
                        .anyMatch(WebElement::isDisplayed)
                    : findProductRow(productCode).isPresent())
                || isDisplayed(NO_DATA_MESSAGE));
  }

  public boolean isProductPresent(String productCode, String productName) {
    searchProduct(productCode, productName);
    return findProductRow(productCode)
        .filter(
            row ->
                cellText(row, "name")
                    .toLowerCase(Locale.ROOT)
                    .contains(productName.trim().toLowerCase(Locale.ROOT)))
        .isPresent();
  }

  public StockUpdateProductPage openProductForEdit(String productCode) {
    searchProduct(productCode, "");
    WebElement row = requireProductRow(productCode);
    String editUrl = row.findElement(By.cssSelector("a.edit")).getAttribute("href");
    navigateTo(editUrl);
    return new StockUpdateProductPage(driver).waitUntilLoaded();
  }

  public boolean isProductActive(String productCode) {
    searchProduct(productCode, "");
    return Boolean.parseBoolean(
        requireProductRow(productCode)
            .findElement(By.cssSelector("button[role='switch']"))
            .getAttribute("aria-checked"));
  }

  public void setProductActive(String productCode, boolean expectedActive) {
    searchProduct(productCode, "");
    WebElement toggle =
        requireProductRow(productCode).findElement(By.cssSelector("button[role='switch']"));
    boolean currentActive = Boolean.parseBoolean(toggle.getAttribute("aria-checked"));
    if (currentActive == expectedActive) {
      return;
    }

    scrollIntoView(toggle);
    clickByJs(toggle);
    waitForLoadingToFinish();
    waitUntilCondition(
        webDriver ->
            findProductRow(productCode)
                .map(
                    row ->
                        Boolean.parseBoolean(
                                row.findElement(By.cssSelector("button[role='switch']"))
                                    .getAttribute("aria-checked"))
                            == expectedActive)
                .orElse(false));
  }

  public boolean deleteProductIfPresent(String productCode) {
    open();
    searchProduct(productCode, "");
    Optional<WebElement> matchingRow = findProductRow(productCode);
    return deleteMatchingRow(matchingRow, productRow(productCode));
  }

  public boolean deleteProductByNameIfPresent(String productName) {
    open();
    searchProduct("", productName);
    Optional<WebElement> matchingRow =
        driver.findElements(PRODUCT_ITEM).stream()
            .filter(WebElement::isDisplayed)
            .filter(row -> cellText(row, "name").equalsIgnoreCase(productName.trim()))
            .findFirst();
    By matchingName =
        By.xpath(
            "//app-product//tbody/tr[normalize-space("
                + "td[contains(@class,'cdk-column-name')])="
                + xpathLiteral(productName.trim())
                + "]");
    return deleteMatchingRow(matchingRow, matchingName);
  }

  private boolean deleteMatchingRow(Optional<WebElement> matchingRow, By rowLocator) {
    if (matchingRow.isEmpty()) {
      return false;
    }

    WebElement deleteAction = matchingRow.get().findElement(By.cssSelector(".action-cell .delete"));
    scrollIntoView(deleteAction);
    clickByJs(deleteAction);
    confirmDeleteIfRequired(rowLocator);
    waitForLoadingToFinish();
    waitUntilCondition(webDriver -> webDriver.findElements(rowLocator).isEmpty());
    return true;
  }

  public void clickStockDropdownMenu() {
    waitUntilVisible(STOCK_MENU_DROPDOWN);
    clickByJs(STOCK_MENU_DROPDOWN);
  }

  public boolean isStockDropdownMenuShowed() {
    waitUntilVisible(STOCK_MENU);
    return isDisplayed(STOCK_MENU);
  }

  public StockOrderPage clickStockOrders() {
    waitUntilVisible(ORDERS_ITEM);
    clickByJs(ORDERS_ITEM);
    return new StockOrderPage(driver);
  }

  public void waitUntilLoadingInvisible() {
    waitUntilInvisible(LOADING_OVERLAY);
  }

  public void enterKeyword() {
    sendEnterKey(PRODUCT_SEARCH_INPUT);
  }

  public void clickSynchroniseButton() {
    clickByJs(SYNCHRONISE_BUTTON);
  }

  public boolean isTicketingAndStockSynchroniseMessageDisplayed() {
    waitUntilVisible(TICKETING_SYNCHRONIZED_MESSAGE);
    waitUntilVisible(STOCK_SYNCHRONIZED_MESSAGE);
    boolean isTicketingSynchroniseMessageMatched =
        textOf(TICKETING_SYNCHRONIZED_MESSAGE).equals(TICKETING_SYNCHRONISE_SUCCESSFULLY);
    boolean isStockSynchroniseMessageMatched =
        textOf(STOCK_SYNCHRONIZED_MESSAGE).equals(STOCK_SYNCHRONISE_SUCCESSFULLY);
    return isStockSynchroniseMessageMatched && isTicketingSynchroniseMessageMatched;
  }

  public boolean isCurrentURLEqualStockProductsPage() {
    return currentUrl().equals(config.baseUrl() + "stock/products");
  }

  public boolean isMainSectionShowed() {
    visible(PRODUCT_SECTION);
    return isDisplayed(PRODUCT_SECTION);
  }

  public void clickOnShopifyButton() {
    clickByJs(SHOPIFY_SYNC_BUTTON);
  }

  public boolean isMessageShowed() {
    waitUntilVisible(SHOPIFY_SUCCESS_TOAST);
    return isDisplayed(SHOPIFY_SUCCESS_TOAST);
  }

  private static By syncToastTitle(String title) {
    return By.xpath(
        "//app-sync-toast//*[contains(@class,'toast-title') and normalize-space()="
            + "'"
            + title
            + "']");
  }

  private Optional<WebElement> findProductRow(String productCode) {
    return driver.findElements(productRow(productCode)).stream()
        .filter(WebElement::isDisplayed)
        .findFirst();
  }

  private WebElement requireProductRow(String productCode) {
    return findProductRow(productCode)
        .orElseThrow(
            () -> new NoSuchElementException("Product row was not found for code: " + productCode));
  }

  private void confirmDeleteIfRequired(By rowLocator) {
    try {
      Alert alert = driver.switchTo().alert();
      alert.accept();
      return;
    } catch (NoAlertPresentException ignored) {
      // The application normally uses a DOM dialog instead of a browser alert.
    }

    waitUntilCondition(
        webDriver ->
            webDriver.findElements(rowLocator).isEmpty()
                || webDriver.findElements(DELETE_CONFIRMATION_BUTTON).stream()
                    .anyMatch(WebElement::isDisplayed));
    if (driver.findElements(rowLocator).isEmpty()) {
      return;
    }

    WebElement confirmation =
        driver.findElements(DELETE_CONFIRMATION_BUTTON).stream()
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(
                () -> new NoSuchElementException("Delete confirmation button was not found"));
    clickByJs(confirmation);
  }

  private static String cellText(WebElement row, String column) {
    return row.findElement(By.cssSelector("td.cdk-column-" + column)).getText().trim();
  }

  private static By productRow(String productCode) {
    return By.xpath(
        "//app-product//tbody/tr[normalize-space("
            + "td[contains(@class,'cdk-column-code')])="
            + xpathLiteral(productCode.trim())
            + "]");
  }

  private static String xpathLiteral(String value) {
    if (!value.contains("'")) {
      return "'" + value + "'";
    }
    if (!value.contains("\"")) {
      return "\"" + value + "\"";
    }
    return "concat('" + value.replace("'", "',\"'\",'") + "')";
  }

  private void waitForLoadingToFinish() {
    waitUntilInvisible(LOADING_OVERLAY);
  }
}
