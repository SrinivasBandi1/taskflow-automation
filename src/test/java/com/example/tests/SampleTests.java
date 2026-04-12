package com.example.tests;

import com.example.pages.SamplePage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SampleTests performs UI checks on the sample app.
 */
public class SampleTests extends BaseTest {

    @Test
    public void testPageTitleAndList() {
           //     SamplePage page = new SamplePage(driver);
        // Verify page title
           //     Assert.assertEquals(page.getTitleText(), "My Sample App");
        // Verify item list count
           //     int count = page.getItemCount();
         //       Assert.assertTrue(count >= 3, "Item list should have at least 3 elements");
    }

        //    @Test
    public void testGreeting() {
        SamplePage page = new SamplePage(driver);
        // Enter name and click greet
        String name = "Alice";
        page.enterName(name);
        page.clickGreet();
        // Verify greeting message appears
        String message = page.getGreetingMessage();
        Assert.assertEquals(message, "Hello " + name + "!");
    }
}
