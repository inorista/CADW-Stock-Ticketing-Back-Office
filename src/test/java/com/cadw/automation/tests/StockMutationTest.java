package com.cadw.automation.tests;

import com.cadw.automation.base.AuthenticatedTest;
import com.cadw.automation.pages.stock.StockAddOrderDetailsPage;
import com.cadw.automation.pages.stock.StockAddOrderPage;
import com.cadw.automation.pages.stock.StockAddProductPage;
import com.cadw.automation.pages.stock.StockOrderPage;
import com.cadw.automation.pages.stock.StockProductsPage;
import com.cadw.automation.pages.stock.StockUpdateProductPage;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StockMutationTest extends AuthenticatedTest {

    @Test(groups = { "testng", "authenticated", "mutation", "product-lifecycle" })
    public void completeProductLifecycle() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String productCode = "TNG" + suffix;
        String productName = "TestNG Product " + suffix;
        String updatedName = productName + " Updated";
        String updatedPrice = "12.50";

        StockProductsPage productsPage = new StockProductsPage(driver()).open();
        StockAddProductPage addProduct = productsPage.clickAddButton();
        Assert.assertTrue(addProduct.isAddProductPageDisplayed(), "Add Product page is unavailable");
        fillProduct(addProduct, productCode, productName);
        productsPage = addProduct.saveAndWaitForProducts();
        Assert.assertTrue(productsPage.isProductPresent(productCode, productName),
                "Created product is not searchable");

        StockUpdateProductPage updatePage = productsPage.openProductForEdit(productCode);
        updatePage.enterProductName(updatedName);
        productsPage = updatePage.saveProductDetails();
        updatePage = productsPage.openProductForEdit(productCode);
        updatePage.updateFirstPrice(updatedPrice);
        productsPage = updatePage.savePriceChanges();

        updatePage = productsPage.openProductForEdit(productCode);
        Assert.assertEquals(updatePage.getProductName(), updatedName,
                "Updated product name was not persisted");
        Assert.assertTrue(updatePage.isFirstPrice(updatedPrice),
                "Updated product price was not persisted");

        productsPage = new StockProductsPage(driver()).open();
        productsPage.setProductActive(productCode, false);
        Assert.assertFalse(productsPage.isProductActive(productCode), "Product is still active");
        Assert.assertTrue(productsPage.deleteProductIfPresent(productCode), "Product was not deleted");
        Assert.assertFalse(productsPage.isProductPresent(productCode, updatedName),
                "Deleted product is still searchable");
    }

    @Test(groups = { "testng", "authenticated", "mutation", "order" })
    public void addOrderForDefaultSupplier() {
        StockProductsPage productsPage = new StockProductsPage(driver()).open();
        productsPage.clickStockDropdownMenu();
        Assert.assertTrue(productsPage.isStockDropdownMenuShowed(), "Stock menu is not displayed");

        StockOrderPage orderPage = productsPage.clickStockOrders();
        Assert.assertTrue(orderPage.isOrderPageDisplayed(), "Orders page is unavailable");
        orderPage.clickAddOrderButton();

        StockAddOrderPage addOrderPage = new StockAddOrderPage(driver());
        Assert.assertTrue(addOrderPage.verifyIfCurrentPageIsStockAddOrderPage(),
                "Add Order page is unavailable");
        addOrderPage.enterComments("TestNG automated order");
        addOrderPage.enterSearchForSupplier("Default Supplier");
        addOrderPage.selectSupplierByName("Default Supplier");

        StockAddOrderDetailsPage detailsPage = addOrderPage.clickNext();
        Assert.assertTrue(detailsPage.verifyIfCurrentPageIsStockAddDetailsPage(),
                "Order Details page is unavailable");
        detailsPage.clickSave();
        Assert.assertTrue(new StockOrderPage(driver()).isOrderPageDisplayed(),
                "Browser did not return to the Orders page");
    }

    @Test(groups = { "testng", "authenticated", "mutation", "sync" })
    public void synchronizeShopify() {
        StockProductsPage productsPage = new StockProductsPage(driver()).open();
        productsPage.clickOnShopifyButton();
        Assert.assertTrue(productsPage.isMessageShowed(), "Shopify success message is not displayed");
    }

    @Test(groups = { "testng", "authenticated", "mutation", "sync" })
    public void synchronizeStockAndTicketing() {
        StockProductsPage productsPage = new StockProductsPage(driver()).open();
        productsPage.clickSynchroniseButton();
        Assert.assertTrue(productsPage.isTicketingAndStockSynchroniseMessageDisplayed(),
                "Synchronization success messages are not displayed");
    }

    private static void fillProduct(
            StockAddProductPage page, String productCode, String productName) {
        page.enterProductCode(productCode);
        page.enterProductName(productName);
        page.enterProductPrintText(productName);
        page.selectProductGroup("Admissions");
        page.selectProductType("All");
        page.enterCommission("10");
        page.enterOrder("10");
        page.selectAllergen("Fish");
        page.selectActive("Yes");
        page.enterMaximumDiscount("10");
        page.selectPrintOptions("Do not print");
        page.selectPurchasingVAT("Standard");
        page.enterPriceType("Regular");
        page.enterPriceLevel("Standard");
        page.enterTaxGroup("Standard");
        page.enterPrice("10");
        page.enterCost("10");
        page.selectValidFromDate();
        page.clickAddPrice();
        page.userSearchOrSelectAnExportCode("test");
        page.clickAutoGenerateBarcode();
        page.clickAddBarcode();
    }
}
