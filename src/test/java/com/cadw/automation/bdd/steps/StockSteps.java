package com.cadw.automation.bdd.steps;

import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.pages.stock.StockAddOrderDetailsPage;
import com.cadw.automation.pages.stock.StockAddOrderPage;
import com.cadw.automation.pages.stock.StockAddProductPage;
import com.cadw.automation.pages.stock.StockOrderPage;
import com.cadw.automation.pages.stock.StockProductsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class StockSteps {
    private StockProductsPage productsPage;
    private StockAddProductPage addProductPage;
    private StockOrderPage orderPage;
    private StockAddOrderPage addOrderPage;
    private StockAddOrderDetailsPage addOrderDetailsPage;

    @Given("User is on Stock Products Page")
    public void openStockProductsPage() {
        productsPage = new StockProductsPage(DriverSession.driver()).open();
        Assert.assertTrue(productsPage.isProductsPageAvailable(), "Stock Products page is unavailable");
    }

    @When("User click Stock dropdown")
    public void clickStockDropdown() {
        productsPage.clickStockDropdownMenu();
    }

    @Then("Stock dropdown show menu")
    public void verifyStockDropdown() {
        Assert.assertTrue(productsPage.isStockDropdownMenuShowed(), "Stock menu is not displayed");
    }

    @When("User click Orders")
    public void clickOrders() {
        orderPage = productsPage.clickStockOrders();
    }

    @Then("User is on Stock Orders Page")
    public void verifyOrdersPage() {
        Assert.assertTrue(orderPage.isCurrentURLIsOrderPage(), "Current URL is not the Orders URL");
        Assert.assertTrue(orderPage.isOrderPageDisplayed(), "Orders page is not displayed");
    }

    @When("User click Add Order button")
    public void clickAddOrder() {
        orderPage.clickAddOrderButton();
        addOrderPage = new StockAddOrderPage(DriverSession.driver());
    }

    @Then("User is on Stock Orders Add Page")
    public void verifyAddOrderPage() {
        Assert.assertTrue(addOrderPage.verifyIfCurrentPageIsStockAddOrderPage(),
                "Add Order page is not displayed");
    }

    @When("User add Order comments {string}")
    public void enterOrderComments(String comments) {
        addOrderPage.enterComments(comments);
    }

    @When("User search for supplier {string}")
    public void searchSupplier(String supplier) {
        addOrderPage.enterSearchForSupplier(supplier);
    }

    @When("User select suppliers {string}")
    public void selectSupplier(String supplier) {
        addOrderPage.selectSupplierByName(supplier);
    }

    @When("User click Next")
    public void clickNext() {
        addOrderDetailsPage = addOrderPage.clickNext();
    }

    @Then("User is on Stock Add Order Details Page")
    public void verifyAddOrderDetailsPage() {
        Assert.assertTrue(addOrderDetailsPage.verifyIfCurrentPageIsStockAddDetailsPage(),
                "Add Order Details page is not displayed");
    }

    @Then("User click Save")
    public void saveOrder() {
        addOrderDetailsPage.clickSave();
        orderPage = new StockOrderPage(DriverSession.driver());
    }

    @Then("User go back to Stock Order Page")
    public void verifyReturnToOrderPage() {
        verifyOrdersPage();
    }

    @When("User click Add button")
    public void clickAddProduct() {
        addProductPage = productsPage.clickAddButton();
    }

    @Then("User navigate to Add Product Page")
    public void verifyAddProductPage() {
        Assert.assertTrue(addProductPage.isAddProductPageDisplayed(), "Add Product page is unavailable");
    }

    @Then("User enter Product Code {string}")
    public void enterProductCode(String value) {
        addProductPage.enterProductCode(value);
    }

    @Then("User enter Product name {string}")
    public void enterProductName(String value) {
        addProductPage.enterProductName(value);
    }

    @Then("User enter Product print text {string}")
    public void enterProductPrintText(String value) {
        addProductPage.enterProductPrintText(value);
    }

    @Then("User select Product group {string}")
    public void selectProductGroup(String value) {
        addProductPage.selectProductGroup(value);
    }

    @Then("User select Product Type {string}")
    public void selectProductType(String value) {
        addProductPage.selectProductType(value);
    }

    @Then("User enter Commission {string}")
    public void enterCommission(String value) {
        addProductPage.enterCommission(value);
    }

    @Then("User enter Order {string}")
    public void enterOrder(String value) {
        addProductPage.enterOrder(value);
    }

    @Then("User select Allergen {string}")
    public void selectAllergen(String value) {
        addProductPage.selectAllergen(value);
    }

    @Then("User select Active {string}")
    public void selectActive(String value) {
        addProductPage.selectActive(value);
    }

    @Then("User enter Maximum discount {string}")
    public void enterMaximumDiscount(String value) {
        addProductPage.enterMaximumDiscount(value);
    }

    @Then("User select Print options {string}")
    public void selectPrintOptions(String value) {
        addProductPage.selectPrintOptions(value);
    }

    @Then("User select Purchasing VAT {string}")
    public void selectPurchasingVat(String value) {
        addProductPage.selectPurchasingVAT(value);
    }

    @Then("User enter Price Type {string}")
    public void enterPriceType(String value) {
        addProductPage.enterPriceType(value);
    }

    @Then("User enter Price Level {string}")
    public void enterPriceLevel(String value) {
        addProductPage.enterPriceLevel(value);
    }

    @Then("User enter Tax Group {string}")
    public void enterTaxGroup(String value) {
        addProductPage.enterTaxGroup(value);
    }

    @Then("User enter Price {string}")
    public void enterPrice(String value) {
        addProductPage.enterPrice(value);
    }

    @Then("User enter Cost {string}")
    public void enterCost(String value) {
        addProductPage.enterCost(value);
    }

    @Then("User select Valid From Date")
    public void selectValidFromDate() {
        addProductPage.selectValidFromDate();
    }

    @Then("User click Add")
    public void addPrice() {
        addProductPage.clickAddPrice();
    }

    @Then("User Search or select an export code {string}")
    public void selectExportCode(String value) {
        addProductPage.userSearchOrSelectAnExportCode(value);
    }

    @Then("User click Auto generate Barcode")
    public void generateBarcode() {
        addProductPage.clickAutoGenerateBarcode();
    }

    @Then("User click Add Barcode")
    public void addBarcode() {
        addProductPage.clickAddBarcode();
    }

    @Then("user click Save")
    public void saveProduct() {
        productsPage = addProductPage.saveAndWaitForProducts();
    }

    @When("User enter {string} to Product name search bar")
    public void enterProductNameSearch(String value) {
        productsPage.enterKeywordToProductNameSearchBar(value);
    }

    @When("User enter {string} to Product code search bar")
    public void enterProductCodeSearch(String value) {
        productsPage.enterKeywordToProductCodeSearchBar(value);
    }

    @When("User send Enter key")
    public void sendEnter() {
        productsPage.sendEnterKey();
    }

    @Then("Product list should show products matching {string} and {string}")
    public void verifyProductList(String productName, String productCode) {
        Assert.assertTrue(productsPage.validateProductList(productName, productCode),
                "No matching product was found");
    }

    @Then("Page should show {string} message")
    public void verifyNoDataMessage(String ignoredMessage) {
        Assert.assertTrue(productsPage.validateNoDataMessage(), "No-data message is not displayed");
    }

    @When("User clicks Shopify sync button")
    public void clickShopifySync() {
        productsPage.clickOnShopifyButton();
    }

    @Then("Show Shopify sync successful")
    public void verifyShopifySync() {
        Assert.assertTrue(productsPage.isMessageShowed(), "Shopify success message is not displayed");
    }

    @When("User clicks Stock Synchronise button")
    public void clickStockSynchronise() {
        productsPage.clickSynchroniseButton();
    }

    @Then("Show Ticketing and Stock Synchronized")
    public void verifyStockSynchronise() {
        Assert.assertTrue(productsPage.isTicketingAndStockSynchroniseMessageDisplayed(),
                "Stock synchronization messages are not displayed");
    }
}
