package com.cadw.automation.base;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Listeners;

import com.cadw.automation.config.EnvironmentConfig;
import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.reporting.TestReportListener;

@Listeners(TestReportListener.class)
public abstract class BaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

    @BeforeTest(alwaysRun = true)
    public void logTestStart(ITestContext context) {
        EnvironmentConfig config = EnvironmentConfig.active();
        String browser = testParameter(context, "browser", config.browser());
        String execution = testParameter(context, "execution", config.getString("execution"));
        LOG.info(
                "[TESTNG][BEFORE_TEST] suite=\"{}\" test=\"{}\" environment={} browser={} execution={}",
                context.getSuite().getName(),
                context.getName(),
                config.environment(),
                browser,
                execution);
    }

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

    private static String testParameter(ITestContext context, String name, String defaultValue) {
        String value = context.getCurrentXmlTest().getParameter(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
