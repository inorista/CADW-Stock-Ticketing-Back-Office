package com.cadw.automation.pages.home;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.cadw.automation.pages.BasePage;

public class HomePage extends BasePage {
  public HomePage(WebDriver driver) {
    super(driver);
  }

  private static final By YOUR_STOCK =
      By.cssSelector("img[src='../../../assets/images/dashboard/your-stock.png']");
  private static final By YOUR_TICKETING =
      By.cssSelector("img[src='../../../assets/images/dashboard/your-ticketing.png']");
  private static final By YOUR_REPORTS =
      By.cssSelector("img[src='../../../assets/images/dashboard/your-reports.png']");
  private static final By YOUR_CUSTOMER =
      By.cssSelector("img[src='../../../assets/images/dashboard/your-customers.png']");
  private static final By PARTNER =
      By.cssSelector("img[src='../../../assets/images/dashboard/your-partner.png']");
  private static final By YOUR_STOCK_SETUP =
      By.cssSelector("img[src='../../../assets/images/dashboard/your-stock-setup.png']");
  private static final By YOUR_TICKETING_SETUP =
      By.cssSelector("img[src='../../../assets/images/dashboard/your-ticketing-setup.png']");
  private static final By AUTHENTICATED_LAYOUT = By.cssSelector("app-layout");
  private static final By STOCK_DASHBOARD_TILE =
      By.cssSelector("app-home img[src*='dashboard/your-stock.png']");

  public void open() {
    driver.get(config.baseUrl());
  }

  public String getBaseUrl() {
    return config.baseUrl();
  }

  public boolean isDashboardDisplayed() {
    waitUntilVisible(AUTHENTICATED_LAYOUT);
    waitUntilVisible(STOCK_DASHBOARD_TILE);
    return isElementDisplayed(AUTHENTICATED_LAYOUT) && isElementDisplayed(STOCK_DASHBOARD_TILE);
  }

  public String getCurrentUrl() {
    return driver.getCurrentUrl();
  }
}
