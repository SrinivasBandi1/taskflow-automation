package com.qa.config;

/**
 * ConfigManager
 *
 * Centralises all runtime configuration.
 * Values are injected as JVM system properties by Maven Surefire,
 * which in turn reads them from the GitHub Actions workflow inputs
 * passed via the repository_dispatch payload.
 *
 * Priority: System property → default fallback
 */
public class ConfigManager {

    private ConfigManager() { /* utility class – no instantiation */ }

    /* ------------------------------------------------------------------ */
    /*  Core parameters sent in the repository_dispatch payload            */
    /* ------------------------------------------------------------------ */

    /**
     * URL of the application under test.
     * Injected as -Dapp.url in CI; falls back to localhost for local runs.
     */
    public static String getAppUrl() {
        return System.getProperty("app.url", "http://localhost:8080");
    }

    /**
     * Browser to run tests on.  Supported: chrome, firefox, edge.
     */
    public static String getBrowser() {
        return System.getProperty("browser", "chrome").toLowerCase().trim();
    }

    /**
     * Deployment environment label, e.g. staging, uat, production.
     */
    public static String getEnv() {
        return System.getProperty("env", "staging");
    }

    /**
     * Build number from the CI pipeline that triggered this run.
     */
    public static String getBuildNumber() {
        return System.getProperty("build.number", "local");
    }

    /**
     * GitHub actor / workflow that triggered the dispatch.
     */
    public static String getTriggeredBy() {
        return System.getProperty("triggered.by", "manual");
    }

    /* ------------------------------------------------------------------ */
    /*  WebDriver configuration                                             */
    /* ------------------------------------------------------------------ */

    /**
     * Whether to run in headless mode.
     * Always true in CI; can be overridden locally.
     */
    public static boolean isHeadless() {
        String val = System.getProperty("headless", "true");
        return Boolean.parseBoolean(val);
    }

    /** Default implicit wait in seconds (use explicit waits in tests). */
    public static int getImplicitWait() {
        return Integer.parseInt(System.getProperty("implicit.wait", "0"));
    }

    /** Page-load timeout in seconds. */
    public static int getPageLoadTimeout() {
        return Integer.parseInt(System.getProperty("page.load.timeout", "30"));
    }

    /** Explicit wait timeout in seconds (used in FluentWait helpers). */
    public static int getExplicitWait() {
        return Integer.parseInt(System.getProperty("explicit.wait", "15"));
    }
}
