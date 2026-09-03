package com.cadw.automation.bdd.steps;

import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.pages.stock.StockAddProductPage;
import com.cadw.automation.pages.stock.StockProductsPage;
import com.cadw.automation.pages.stock.StockUpdateProductPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.UUID;
import org.testng.Assert;

public class StockE2ESteps {
    private StockProductsPage productsPage;
    private String productCode;
    private String productName;
    private String updatedProductName;
    private String updatedPrice;

    @When("the user opens the Stock landing route")
    public void openStockLanding() {
        productsPage = new StockProductsPage(DriverSession.driver()).openStockLanding();
    }

    @Then("the Stock landing route should provide a usable page")
    public void verifyStockLanding() {
        Assert.assertTrue(productsPage.isStockLandingAvailable(), "Stock landing page is unavailable");
    }

    @When("the user opens the Stock Products page")
    public void openStockProducts() {
        productsPage = new StockProductsPage(DriverSession.driver()).open();
    }

    @Then("the Stock Products page should be available")
    public void verifyStockProducts() {
        Assert.assertTrue(productsPage.isProductsPageAvailable(), "Stock Products page is unavailable");
    }

    @Given("unique data for a Stock product lifecycle")
    public void createUniqueProductData() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        productCode = "E2E" + suffix;
        productName = "E2E Product " + suffix;
        updatedProductName = productName + " Updated";
        updatedPrice = "12.50";
        productsPage = new StockProductsPage(DriverSession.driver()).open();
        productsPage.deleteProductIfPresent(productCode);
    }

    @When("the user creates the Stock product with price and barcode")
    public void createProduct() {
        StockAddProductPage addProduct = productsPage.clickAddButton();
        Assert.assertTrue(addProduct.isAddProductPageDisplayed(), "Add Product page is unavailable");
        addProduct.enterProductCode(productCode);
        addProduct.enterProductName(productName);
        addProduct.enterProductPrintText(productName);
        addProduct.selectProductGroup("Admissions");
        addProduct.selectProductType("All");
        addProduct.enterCommission("10");
        addProduct.enterOrder("10");
        addProduct.selectAllergen("Fish");
        addProduct.selectActive("Yes");
        addProduct.enterMaximumDiscount("10");
        addProduct.selectPrintOptions("Do not print");
        addProduct.selectPurchasingVAT("Standard");
        addProduct.enterPriceType("Regular");
        addProduct.enterPriceLevel("Standard");
        addProduct.enterTaxGroup("Standard");
        addProduct.enterPrice("10");
        addProduct.enterCost("10");
        addProduct.selectValidFromDate();
        addProduct.clickAddPrice();
        addProduct.userSearchOrSelectAnExportCode("test");
        addProduct.clickAutoGenerateBarcode();
        addProduct.clickAddBarcode();
        productsPage = addProduct.saveAndWaitForProducts();
    }

    @Then("the created Stock product should be searchable")
    public void verifyCreatedProduct() {
        Assert.assertTrue(productsPage.isProductPresent(productCode, productName),
                "Created product is not searchable: " + productCode);
    }

    @When("the user updates the Stock product name and price")
    public void updateProduct() {
        StockUpdateProductPage updatePage = productsPage.openProductForEdit(productCode);
        updatePage.enterProductName(updatedProductName);
        productsPage = updatePage.saveProductDetails();

        updatePage = productsPage.openProductForEdit(productCode);
        updatePage.updateFirstPrice(updatedPrice);
        productsPage = updatePage.savePriceChanges();
    }

    @Then("the updated Stock product name and price should be persisted")
    public void verifyUpdatedProduct() {
        StockUpdateProductPage updatePage = productsPage.openProductForEdit(productCode);
        Assert.assertEquals(updatePage.getProductName(), updatedProductName,
                "Updated product name was not persisted");
        Assert.assertTrue(updatePage.isFirstPrice(updatedPrice),
                "Updated product price was not persisted");
        productsPage = new StockProductsPage(DriverSession.driver()).open();
    }

    @When("the user deactivates the Stock product")
    public void deactivateProduct() {
        productsPage.setProductActive(productCode, false);
    }

    @Then("the Stock product should be inactive")
    public void verifyProductInactive() {
        Assert.assertFalse(productsPage.isProductActive(productCode),
                "Product is still active: " + productCode);
    }

    @When("the user deletes the Stock product")
    public void deleteProduct() {
        Assert.assertTrue(productsPage.deleteProductIfPresent(productCode),
                "Product could not be deleted: " + productCode);
    }

    @Then("the Stock product should no longer be searchable")
    public void verifyProductDeleted() {
        Assert.assertFalse(productsPage.isProductPresent(productCode, updatedProductName),
                "Deleted product is still searchable: " + productCode);
    }
}
