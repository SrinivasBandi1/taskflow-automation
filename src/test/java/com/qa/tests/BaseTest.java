package com.qa.tests;

import com.qa.config.ConfigManager;
import com.qa.utils.DriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BaseTest
 *
 * All test classes extend this.
 *
 * Responsibilities:
 *  1. Initialise WebDriver before each test method (@BeforeMethod)
 *  2. Quit WebDriver after each test method (@AfterMethod)
 *  3. Capture screenshot on failure and attach to Allure report
 *  4. Write allure.properties with environment metadata (build, env, URL)
 *  5. Log suite/test start for CI log readability
 */
public abstract class BaseTest {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

    /* ------------------------------------------------------------------ */
    /*  Suite-level lifecycle                                               */
    /* ------------------------------------------------------------------ */

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        LOG.info("========================================================");
        LOG.info("  TaskFlow Automation Suite Starting");
        LOG.info("  Env         : {}", ConfigManager.getEnv());
        LOG.info("  App URL     : {}", ConfigManager.getAppUrl());
        LOG.info("  Browser     : {}", ConfigManager.getBrowser());
        LOG.info("  Build No.   : {}", ConfigManager.getBuildNumber());
        LOG.info("  Triggered By: {}", ConfigManager.getTriggeredBy());
        LOG.info("========================================================");

        // Write Allure environment.properties so the report shows env info
        writeAllureEnvironmentProperties();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        LOG.info("========================================================");
        LOG.info("  TaskFlow Automation Suite Completed");
        LOG.info("========================================================");
    }

    /* ------------------------------------------------------------------ */
    /*  Test-level lifecycle                                                */
    /* ------------------------------------------------------------------ */

    @BeforeMethod(alwaysRun = true)
    public void setUp(java.lang.reflect.Method method) {
        LOG.info("---> Starting test: {}", method.getName());
        DriverManager.initDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            LOG.error("TEST FAILED: {} — capturing screenshot", result.getName());
            captureScreenshotOnFailure();
        }
        DriverManager.quitDriver();
        LOG.info("<--- Finished test: {} | Status: {}",
            result.getName(), statusLabel(result.getStatus()));
    }

    /* ------------------------------------------------------------------ */
    /*  Screenshot on failure                                               */
    /* ------------------------------------------------------------------ */

    @Attachment(value = "Failure Screenshot", type = "image/png")
    private byte[] captureScreenshotOnFailure() {
        try {
            return ((TakesScreenshot) DriverManager.getDriver())
                .getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            LOG.warn("Could not capture screenshot: {}", e.getMessage());
            return new byte[0];
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Allure environment.properties                                       */
    /* ------------------------------------------------------------------ */

    /**
     * Writes allure-results/environment.properties so the Allure report
     * displays a rich "Environment" block — visible to stakeholders.
     */
    private void writeAllureEnvironmentProperties() {
        try {
            Path dir = Paths.get("target", "allure-results");
            Files.createDirectories(dir);

            Path propFile = dir.resolve("environment.properties");
            try (FileWriter fw = new FileWriter(propFile.toFile())) {
                fw.write("Environment=" + ConfigManager.getEnv() + "\n");
                fw.write("App.URL="     + ConfigManager.getAppUrl() + "\n");
                fw.write("Browser="     + ConfigManager.getBrowser() + "\n");
                fw.write("Build.Number="+ ConfigManager.getBuildNumber() + "\n");
                fw.write("Triggered.By="+ ConfigManager.getTriggeredBy() + "\n");
                fw.write("Run.Time="    + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
            }

            LOG.info("Allure environment.properties written to: {}", propFile);
        } catch (IOException e) {
            LOG.warn("Could not write allure environment.properties: {}", e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Helpers                                                             */
    /* ------------------------------------------------------------------ */

    private static String statusLabel(int status) {
        switch (status) {
            case ITestResult.SUCCESS:
                return "PASSED";
            case ITestResult.FAILURE:
                return "FAILED";
            case ITestResult.SKIP:
                return "SKIPPED";
            default:
                return "UNKNOWN";
        }
    }
}
