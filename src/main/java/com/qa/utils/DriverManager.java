package com.qa.utils;

import com.qa.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * DriverManager
 *
 * Thread-safe WebDriver factory using ThreadLocal storage.
 * Supports Chrome, Firefox, and Edge.
 * WebDriverManager handles driver binary downloads automatically — no manual
 * chromedriver/geckodriver setup needed in CI or locally.
 *
 * Usage:
 *   DriverManager.initDriver();          // creates driver for current thread
 *   WebDriver driver = DriverManager.getDriver();
 *   DriverManager.quitDriver();          // cleans up after test
 */
public class DriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(DriverManager.class);

    /** One driver instance per test thread (supports parallel execution). */
    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    private DriverManager() { /* static utility class */ }

    /* ------------------------------------------------------------------ */
    /*  Public API                                                          */
    /* ------------------------------------------------------------------ */

    /**
     * Initialises a WebDriver instance for the current thread.
     * Browser and headless flag are read from {@link ConfigManager}.
     */
    public static void initDriver() {
        String browser  = ConfigManager.getBrowser();
        boolean headless = ConfigManager.isHeadless();

        LOG.info("Initialising [{}] driver | headless={} | env={} | build={}",
                browser, headless, ConfigManager.getEnv(), ConfigManager.getBuildNumber());

        WebDriver driver;
        switch (browser) {
            case "firefox":
                driver = createFirefoxDriver(headless);
                break;
            case "edge":
                driver = createEdgeDriver(headless);
                break;
            case "chrome":
            default:
                driver = createChromeDriver(headless);
                break;
        }

        // Apply timeouts
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getPageLoadTimeout()));
        driver.manage().window().maximize();

        DRIVER_THREAD_LOCAL.set(driver);
        LOG.info("WebDriver initialised successfully for thread [{}]", Thread.currentThread().getId());
    }

    /**
     * Returns the WebDriver for the current thread.
     * Throws if {@link #initDriver()} was not called first.
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver == null) {
            throw new IllegalStateException(
                "WebDriver not initialised for thread " + Thread.currentThread().getId() +
                ". Call DriverManager.initDriver() in @BeforeMethod.");
        }
        return driver;
    }

    /**
     * Quits the WebDriver and clears the ThreadLocal.
     * Must be called in @AfterMethod to prevent driver leaks.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver != null) {
            try {
                driver.quit();
                LOG.info("WebDriver quit for thread [{}]", Thread.currentThread().getId());
            } catch (Exception e) {
                LOG.warn("Exception while quitting WebDriver: {}", e.getMessage());
            } finally {
                DRIVER_THREAD_LOCAL.remove();
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Private factory methods                                             */
    /* ------------------------------------------------------------------ */

    private static ChromeDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        applyCommonFlags(opts, headless);
        opts.addArguments("--disable-blink-features=AutomationControlled");
        opts.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        return new ChromeDriver(opts);
    }

    private static FirefoxDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions opts = new FirefoxOptions();
        if (headless) opts.addArguments("--headless");
        opts.addArguments("--width=1920", "--height=1080");
        return new FirefoxDriver(opts);
    }

    private static EdgeDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions opts = new EdgeOptions();
        applyCommonFlags(opts, headless);
        return new EdgeDriver(opts);
    }

    /**
     * Applies common Chromium-family arguments to both Chrome and Edge.
     */
    private static void applyCommonFlags(org.openqa.selenium.chromium.ChromiumOptions<?> opts, boolean headless) {
        if (headless) {
            opts.addArguments("--headless=new");  // Chromium headless v2 (more stable)
        }
        opts.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--window-size=1920,1080",
            "--disable-gpu",
            "--disable-extensions",
            "--remote-allow-origins=*"
        );
    }
}
