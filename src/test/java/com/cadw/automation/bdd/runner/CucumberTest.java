package com.cadw.automation.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@CucumberOptions(features = "classpath:features", glue = "com.cadw.automation.bdd", plugin = { "pretty", "summary",
    "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" }, monochrome = true)
@Test
public class CucumberTest extends AbstractTestNGCucumberTests {

  @Override
  @DataProvider(parallel = false)
  public Object[][] scenarios() {
    return super.scenarios();
  }
}
