package com.cadw.automation.tests;

import com.cadw.automation.base.AuthenticatedTest;
import com.cadw.automation.pages.stock.StockProductsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StockReadOnlyTest extends AuthenticatedTest {

    @Test(groups = { "testng", "authenticated", "smoke", "read-only" })
    public void stockLandingAndProductsPagesAreAvailable() {
        StockProductsPage productsPage = new StockProductsPage(driver()).openStockLanding();
        Assert.assertTrue(productsPage.isStockLandingAvailable(), "Stock landing page is unavailable");

        productsPage.open();
        Assert.assertTrue(productsPage.isProductsPageAvailable(), "Stock Products page is unavailable");
    }

    @Test(
            dataProvider = "knownProducts",
            groups = { "testng", "authenticated", "smoke", "read-only", "product-search" })
    public void findKnownProduct(String productName, String productCode) {
        StockProductsPage productsPage = new StockProductsPage(driver()).open();
        productsPage.enterKeywordToProductNameSearchBar(productName);
        productsPage.enterKeywordToProductCodeSearchBar(productCode);
        productsPage.sendEnterKey();

        Assert.assertTrue(productsPage.validateProductList(productName, productCode),
                "Known product was not found: " + productCode);
    }

    @Test(
            dataProvider = "unknownProducts",
            groups = { "testng", "authenticated", "smoke", "read-only", "product-search" })
    public void showNoDataForUnknownProduct(String productName, String productCode) {
        StockProductsPage productsPage = new StockProductsPage(driver()).open();
        productsPage.enterKeywordToProductNameSearchBar(productName);
        productsPage.enterKeywordToProductCodeSearchBar(productCode);
        productsPage.sendEnterKey();

        Assert.assertTrue(productsPage.validateNoDataMessage(),
                "No-data message is not displayed for unknown product");
    }

    @DataProvider(name = "knownProducts")
    public Object[][] knownProducts() {
        return new Object[][] {
                { "ShopifyTest", "ShopifyTest" },
                { "Dan TEst 10", "1915" }
        };
    }

    @DataProvider(name = "unknownProducts")
    public Object[][] unknownProducts() {
        return new Object[][] {
                { "Test product 213123", "12366" },
                { "TRest Fialed product", "123572" }
        };
    }
}
