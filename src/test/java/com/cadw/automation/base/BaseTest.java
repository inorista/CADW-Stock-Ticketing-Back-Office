package com.cadw.automation.base;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Listeners;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.reporting.TestReportListener;

@Listeners(TestReportListener.class)
public abstract class BaseTest {
    @BeforeMethod(alwaysRun = true)
    @Parameters({ "browser", "execution" })
    public void setUp(
            @Optional("") String browser,
            @Optional("") String execution,
            Method testMethod) {
        DriverFactory.start(browser, execution, testMethod.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            DriverFactory.markRemoteStatus(
                    result.getStatus() != ITestResult.FAILURE,
                    result.getMethod().getMethodName() + " - " + statusText(result));
        } finally {
            boolean authenticated = Arrays.asList(result.getMethod().getGroups())
                    .contains("authenticated");
            DriverFactory.stop(authenticated);
        }
    }

    protected WebDriver driver() {
        return DriverSession.driver();
    }

    protected String baseUrl() {
        return EnvironmentConfig.active().baseUrl();
    }

    private static String statusText(ITestResult result) {
        return result.getThrowable() == null ? "passed" : result.getThrowable().getMessage();
    }
}
