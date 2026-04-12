package com.example.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model for the Sample App page.
 */
public class SamplePage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By titleBy = By.id("app-title");
    private By nameInputBy = By.id("name-input");
    private By greetButtonBy = By.id("greet-button");
    private By messageBy = By.id("greet-message");
    private By itemListBy = By.id("item-list");

    public SamplePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getTitleText() {
        // Wait for title to be visible
        return wait.until(ExpectedConditions.visibilityOfElementLocated(titleBy)).getText();
    }

    public void enterName(String name) {
        driver.findElement(nameInputBy).sendKeys(name);
    }

    public void clickGreet() {
        driver.findElement(greetButtonBy).click();
    }

    public String getGreetingMessage() {
        // Wait for message to appear
        return wait.until(ExpectedConditions.visibilityOfElementLocated(messageBy)).getText();
    }

    public int getItemCount() {
        // Return number of items in the list
        return driver.findElements(By.cssSelector("#item-list li")).size();
    }
}
