package com.cadw.automation.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.nio.file.Path;

public final class ExtentReportManager {
    private static final Path REPORT_PATH = Path.of("target", "extent-report", "index.html");
    private static final ExtentReports REPORT = createReport();

    private ExtentReportManager() {
    }

    public static ExtentReports report() {
        return REPORT;
    }

    private static ExtentReports createReport() {
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH.toString());
        spark.config().setDocumentTitle("Selenium Automation Results");
        spark.config().setReportName("CADW Automation Suite");

        ExtentReports report = new ExtentReports();
        report.attachReporter(spark);
        report.setSystemInfo("Java", System.getProperty("java.version"));
        report.setSystemInfo("OS", System.getProperty("os.name"));
        return report;
    }
}
