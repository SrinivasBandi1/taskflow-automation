package com.qa.pages;

import com.qa.utils.DriverManager;
import com.qa.utils.WaitHelper;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BasePage
 *
 * All Page Object classes extend this.  Provides:
 *  - Driver access via DriverManager (no driver param in constructors)
 *  - Shared WaitHelper instance
 *  - Common actions: navigate, getTitle, scroll, JS executor
 *  - Allure @Step annotations on shared actions for cleaner reports
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WaitHelper wait;

    protected static final Logger LOG = LoggerFactory.getLogger(BasePage.class);

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait   = new WaitHelper(driver);
        // Initialise @FindBy annotated fields in sub-classes
        PageFactory.initElements(driver, this);
    }

    /* ------------------------------------------------------------------ */
    /*  Navigation                                                          */
    /* ------------------------------------------------------------------ */

    @Step("Navigate to URL: {url}")
    public void navigateTo(String url) {
        LOG.info("Navigating to: {}", url);
        driver.get(url);
    }

    @Step("Get current page title")
    public String getPageTitle() {
        return driver.getTitle();
    }

    @Step("Get current URL")
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /* ------------------------------------------------------------------ */
    /*  Element actions                                                     */
    /* ------------------------------------------------------------------ */

    /**
     * Clears a field and types the given text.
     * Waits for the element to be clickable before interacting.
     */
    @Step("Type '{text}' into field: {locator}")
    protected void typeInto(By locator, String text) {
        WebElement el = wait.waitForClickability(locator);
        el.clear();
        el.sendKeys(text);
    }

    /** Clicks an element after waiting for it to be clickable. */
    @Step("Click element: {locator}")
    protected void click(By locator) {
        wait.waitForClickability(locator).click();
    }

    /** Reads trimmed visible text from an element. */
    protected String getText(By locator) {
        return wait.waitForVisibility(locator).getText().trim();
    }

    /** Checks whether an element is displayed right now (no wait). */
    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    /** Selects an <option> by visible text inside a <select>. */
    protected void selectByVisibleText(By locator, String visibleText) {
        WebElement select = wait.waitForVisibility(locator);
        new org.openqa.selenium.support.ui.Select(select).selectByVisibleText(visibleText);
    }

    /* ------------------------------------------------------------------ */
    /*  JavaScript helpers                                                  */
    /* ------------------------------------------------------------------ */

    /** Scroll an element into view using JavaScript. */
    protected void scrollIntoView(By locator) {
        WebElement el = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    /** Returns the value of a CSS class attribute for an element. */
    protected String getCssClass(By locator) {
        return wait.waitForPresence(locator).getAttribute("class");
    }

    /* ------------------------------------------------------------------ */
    /*  Page readiness                                                      */
    /* ------------------------------------------------------------------ */

    /**
     * Waits until the browser's document.readyState == 'complete'.
     * Useful after page loads or SPA route changes.
     */
    protected void waitForPageLoad() {
        wait.waitForVisibility(By.tagName("body"));
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20))
            .until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }
}
