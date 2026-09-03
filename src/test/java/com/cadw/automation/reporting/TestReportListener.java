package com.cadw.automation.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.cadw.automation.driver.DriverSession;
import com.cadw.automation.reporting.ExtentReportManager;
import com.cadw.automation.utils.ScreenshotUtils;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

public final class TestReportListener implements ITestListener, IExecutionListener {
    private static final Logger LOG = LoggerFactory.getLogger(TestReportListener.class);
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        String displayName = result.getMethod().getMethodName() + parameters(result);
        ExtentTest test = ExtentReportManager.report().createTest(displayName);
        test.assignCategory(result.getMethod().getGroups());
        TEST.set(test);
        LOG.info("START {}", displayName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        current().pass("Passed");
        LOG.info("PASS {}", result.getName());
        TEST.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = current();
        test.fail(result.getThrowable());
        ScreenshotUtils.capture(DriverSession.driverOrNull()).ifPresent(bytes -> {
            Allure.addAttachment("Failure screenshot", "image/png", new ByteArrayInputStream(bytes), ".png");
            String base64 = Base64.getEncoder().encodeToString(bytes);
            test.fail("Browser screenshot", MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
        });
        LOG.error("FAIL {}", result.getName(), result.getThrowable());
        TEST.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (result.getThrowable() == null) {
            current().skip("Skipped");
        } else {
            current().skip(result.getThrowable());
        }
        LOG.warn("SKIP {}", result.getName());
        TEST.remove();
    }

    @Override
    public void onExecutionFinish() {
        ExtentReportManager.report().flush();
    }

    private static ExtentTest current() {
        ExtentTest test = TEST.get();
        if (test == null) {
            test = ExtentReportManager.report().createTest("Unregistered test");
            TEST.set(test);
        }
        return test;
    }

    private static String parameters(ITestResult result) {
        return result.getParameters().length == 0
                ? ""
                : " " + Arrays.deepToString(result.getParameters());
    }
}
