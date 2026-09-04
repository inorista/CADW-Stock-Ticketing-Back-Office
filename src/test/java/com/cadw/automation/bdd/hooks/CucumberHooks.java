package com.cadw.automation.bdd.hooks;

import com.cadw.automation.driver.DriverFactory;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.support.AuthenticationSupport;
import com.cadw.automation.utils.ScreenshotUtils;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.Reporter;

public final class CucumberHooks {
    private static final Logger LOG = LoggerFactory.getLogger(CucumberHooks.class);
    private static final ThreadLocal<Integer> STEP_NUMBER = ThreadLocal.withInitial(() -> 0);

    @Before(order = Integer.MIN_VALUE)
    public void reserveExecutionSlot(Scenario scenario) {
        boolean requiresExclusiveExecution = scenario.getSourceTagNames().contains("@serial")
                || scenario.getSourceTagNames().contains("@mutation");
        ScenarioExecutionGuard.acquire(requiresExclusiveExecution);
        STEP_NUMBER.set(0);
        LOG.info(
                "[CUCUMBER][BEFORE_SCENARIO] scenario=\"{}\" source={}:{} tags={}",
                scenario.getName(),
                scenario.getUri(),
                scenario.getLine(),
                scenario.getSourceTagNames());
    }

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

    @BeforeStep(order = Integer.MIN_VALUE)
    public void logStepStart(Scenario scenario) {
        int stepNumber = STEP_NUMBER.get() + 1;
        STEP_NUMBER.set(stepNumber);
        LOG.info(
                "[CUCUMBER][BEFORE_STEP] step={} scenario=\"{}\" source={}:{}",
                stepNumber,
                scenario.getName(),
                scenario.getUri(),
                scenario.getLine());
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

    @After(order = Integer.MIN_VALUE)
    public void releaseExecutionSlot(Scenario scenario) {
        try {
            LOG.info(
                    "[CUCUMBER][AFTER_SCENARIO] status={} steps={} scenario=\"{}\"",
                    scenario.getStatus(),
                    STEP_NUMBER.get(),
                    scenario.getName());
        } finally {
            STEP_NUMBER.remove();
            ScenarioExecutionGuard.release();
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
