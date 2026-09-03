package com.cadw.automation.bdd.hooks;

import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.support.AuthenticationSupport;
import com.cadw.automation.utils.ScreenshotUtils;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.testng.ITestResult;
import org.testng.Reporter;

public final class CucumberHooks {
    @Before(order = 0)
    public void startBrowser(Scenario scenario) {
        DriverFactory.start(testParameter("browser"), testParameter("execution"), scenario.getName());
        if (scenario.getSourceTagNames().contains("@login")) {
            AuthenticationSupport.resetToLoggedOutState();
        }
    }

    @Before(value = "@authenticated", order = 1)
    public void ensureAuthenticated() {
        AuthenticationSupport.ensureAuthenticated();
    }

    @After
    public void stopBrowser(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                ScreenshotUtils.capture(DriverSession.driverOrNull())
                        .ifPresent(bytes -> scenario.attach(bytes, "image/png", "Failure screenshot"));
            }
            DriverFactory.markRemoteStatus(!scenario.isFailed(), scenario.getName());
        } finally {
            DriverFactory.stop(scenario.getSourceTagNames().contains("@authenticated"));
        }
    }

    private static String testParameter(String name) {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null || result.getTestContext() == null
                || result.getTestContext().getCurrentXmlTest() == null) {
            return "";
        }
        String value = result.getTestContext().getCurrentXmlTest().getParameter(name);
        return value == null ? "" : value;
    }

}
