package com.cadw.automation.data;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(features = "src/test/resources/features", glue = "com.cadw.automation.bdd.steps", plugin = {
        "pretty",
        "html:target/cucumber-report/cucumber.html",
        "json:target/cucumber-report/cucumber.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
}, monochrome = true)
public final class CucumberTestRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
