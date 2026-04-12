package com.example.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;

/**
 * BaseTest sets up the WebDriver based on system properties.
 */
public class BaseTest {
    protected WebDriver driver;

          //  @BeforeClass
    public void setUp() {
        String browser = System.getProperty("browser", "chrome");
        String appUrl = "https://srinivasbandi1.github.io/app-repo/";
        if (browser.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver();
        } else {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
        }
        // Maximize and implicit wait
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        // Navigate to app URL
        driver.get(appUrl);
    }

         //   @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
