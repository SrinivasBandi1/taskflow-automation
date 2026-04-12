package com.qa.tests;

import com.qa.config.ConfigManager;
import com.qa.pages.TaskFlowPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * TaskFlowActivityFeedTest
 *
 * Validates the "Recent Activity" feed section — ensuring it is rendered
 * with the expected pre-seeded items and that item content is non-empty.
 */
@Epic("TaskFlow Pro Application")
@Feature("Activity Feed")
public class TaskFlowActivityFeedTest extends BaseTest {

    private TaskFlowPage taskFlowPage;

    @BeforeMethod(alwaysRun = true)
    public void openApplication() {
        taskFlowPage = new TaskFlowPage();
        taskFlowPage.navigateTo(ConfigManager.getAppUrl());
        taskFlowPage.waitForPageToLoad();
    }

    /* ------------------------------------------------------------------ */
    /*  TC-12: Activity Feed Is Displayed with Pre-Seeded Items             */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-12: Verify the activity feed renders with at least 3 pre-seeded activity entries")
    @Story("Activity Feed")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
        Steps:
         1. Scroll to the activity feed section
         2. Verify the section container is visible
         3. Verify there are at least 3 activity items (matching seeded data)
         4. Verify the first item's text is non-empty
        """)
    public void tc12_activityFeed_showsPreSeededItems() {
        Assert.assertTrue(
            taskFlowPage.isActivityFeedDisplayed(),
            "Activity feed section should be visible on the page"
        );

        int itemCount = taskFlowPage.getActivityItemCount();
        LOG.info("Activity feed item count: {}", itemCount);

        Assert.assertTrue(
            itemCount >= 3,
            "Activity feed should show at least 3 pre-seeded items but found: " + itemCount
        );

        String firstItemText = taskFlowPage.getFirstActivityItemText();
        LOG.info("First activity item text: {}", firstItemText);

        Assert.assertFalse(
            firstItemText.isEmpty(),
            "First activity item should have non-empty text"
        );
    }
}
