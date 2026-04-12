package com.qa.utils;

import com.qa.config.ConfigManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * WaitHelper
 *
 * Centralises all explicit-wait logic so Page Objects stay clean.
 * Uses WebDriverWait (backed by FluentWait) with sensible defaults.
 *
 * All waits read their timeout from ConfigManager so CI can tune them
 * via -Dexplicit.wait=20 without touching code.
 */
public class WaitHelper {

    private static final Logger LOG = LoggerFactory.getLogger(WaitHelper.class);

    private final WebDriver driver;
    private final Duration  timeout;
    private final Duration  pollingInterval = Duration.ofMillis(300);

    public WaitHelper(WebDriver driver) {
        this.driver  = driver;
        this.timeout = Duration.ofSeconds(ConfigManager.getExplicitWait());
    }

    /** Convenience constructor allowing a custom timeout in seconds. */
    public WaitHelper(WebDriver driver, int timeoutSeconds) {
        this.driver  = driver;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    /* ------------------------------------------------------------------ */
    /*  Visibility                                                          */
    /* ------------------------------------------------------------------ */

    /** Wait until the element is visible in the DOM and rendered. */
    public WebElement waitForVisibility(By locator) {
        LOG.debug("Waiting for visibility of: {}", locator);
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Wait until a WebElement (already found) is visible. */
    public WebElement waitForVisibility(WebElement element) {
        return getWait().until(ExpectedConditions.visibilityOf(element));
    }

    /** Wait until the element is invisible (e.g., a loading spinner disappears). */
    public boolean waitForInvisibility(By locator) {
        return getWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /* ------------------------------------------------------------------ */
    /*  Clickability                                                        */
    /* ------------------------------------------------------------------ */

    /** Wait until the element is visible AND enabled (safe to click). */
    public WebElement waitForClickability(By locator) {
        LOG.debug("Waiting for clickability of: {}", locator);
        return getWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    /* ------------------------------------------------------------------ */
    /*  Text / Attribute                                                    */
    /* ------------------------------------------------------------------ */

    /** Wait until element's text contains the expected substring. */
    public boolean waitForTextPresent(By locator, String text) {
        return getWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /** Wait until element's text equals the expected value exactly. */
    public boolean waitForTextToBe(By locator, String expectedText) {
        return getWait()
            .until(driver -> {
                try {
                    String actual = driver.findElement(locator).getText().trim();
                    return actual.equals(expectedText);
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            });
    }

    /** Wait until an element attribute contains a given value. */
    public boolean waitForAttributeContains(By locator, String attribute, String value) {
        return getWait()
            .until(ExpectedConditions.attributeContains(locator, attribute, value));
    }

    /* ------------------------------------------------------------------ */
    /*  Presence                                                            */
    /* ------------------------------------------------------------------ */

    /** Wait until element is present in the DOM (may not be visible). */
    public WebElement waitForPresence(By locator) {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /* ------------------------------------------------------------------ */
    /*  Page title / URL                                                    */
    /* ------------------------------------------------------------------ */

    /** Wait until the page title contains the given substring. */
    public boolean waitForTitleContains(String title) {
        return getWait().until(ExpectedConditions.titleContains(title));
    }

    /** Wait until the current URL contains the given substring. */
    public boolean waitForUrlContains(String urlFragment) {
        return getWait().until(ExpectedConditions.urlContains(urlFragment));
    }

    /* ------------------------------------------------------------------ */
    /*  Number of elements                                                  */
    /* ------------------------------------------------------------------ */

    /** Wait until the count of elements matching a locator is at least n. */
    public boolean waitForMinimumElementCount(By locator, int minimum) {
        return getWait()
            .until(driver -> driver.findElements(locator).size() >= minimum);
    }

    /* ------------------------------------------------------------------ */
    /*  Staleness                                                           */
    /* ------------------------------------------------------------------ */

    /** Wait until the element becomes stale (e.g., after a DOM refresh). */
    public boolean waitForStaleness(WebElement element) {
        return getWait().until(ExpectedConditions.stalenessOf(element));
    }

    /* ------------------------------------------------------------------ */
    /*  Private helpers                                                     */
    /* ------------------------------------------------------------------ */

    private WebDriverWait getWait() {
        return new WebDriverWait(driver, timeout, pollingInterval);
    }
}
