package com.cadw.automation.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(features = "classpath:features/e2e/stock_e2e.feature", glue = "com.cadw.automation.bdd", tags = "@e2e_stk_001 or @e2e_stk_002", plugin = {
    "pretty",
    "summary",
    "html:target/cucumber-report/cucumber.html",
    "json:target/cucumber-report/cucumber.json",
    "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
}, monochrome = true)
public class StockE2ETest extends AbstractTestNGCucumberTests {
  @Override
  @DataProvider(parallel = false)
  public Object[][] scenarios() {
    return super.scenarios();
  }
}
