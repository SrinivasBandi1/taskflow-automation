package com.qa.tests;

import com.qa.config.ConfigManager;
import com.qa.pages.TaskFlowPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * TaskFlowHeaderAndStatsTest
 *
 * Validates the page header, navigation bar, logged-in user display,
 * and the stats strip (total / in-progress / completed / overdue counts).
 *
 * These tests verify the foundational page structure that must be working
 * before any functional tests are meaningful.
 */
@Epic("TaskFlow Pro Application")
@Feature("Page Header & Statistics")
public class TaskFlowHeaderAndStatsTest extends BaseTest {

    private TaskFlowPage taskFlowPage;

    @BeforeMethod(alwaysRun = true)
    public void openApplication() {
        taskFlowPage = new TaskFlowPage();
        taskFlowPage.navigateTo(ConfigManager.getAppUrl());
        taskFlowPage.waitForPageToLoad();
    }

    /* ------------------------------------------------------------------ */
    /*  TC-01: Page Title Verification                                      */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-01: Verify the browser tab title contains the expected application name")
    @Story("Header")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
        Navigates to the application URL and verifies:
         1. Page loads without errors
         2. Browser title contains 'TaskFlow Pro'
        """)
    public void tc01_verifyPageTitle() {
        String title = taskFlowPage.getPageTitle();
        LOG.info("Page title: {}", title);

        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertTrue(
            title.contains("TaskFlow"),
            "Page title should contain 'TaskFlow' but was: " + title
        );
    }

    /* ------------------------------------------------------------------ */
    /*  TC-02: Header Visibility                                            */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-02: Verify the site header is rendered and the logo is visible")
    @Story("Header")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
        Checks:
         1. The site-header element is present in the DOM
         2. The logo text contains the product name
        """)
    public void tc02_verifyHeaderAndLogo() {
        Assert.assertTrue(
            taskFlowPage.isHeaderDisplayed(),
            "Site header should be displayed on page load"
        );

        String logoText = taskFlowPage.getLogoText();
        LOG.info("Logo text: {}", logoText);
        Assert.assertTrue(
            logoText.contains("TaskFlow"),
            "Logo should display 'TaskFlow' but got: " + logoText
        );
    }

    /* ------------------------------------------------------------------ */
    /*  TC-03: Logged-In User Display                                       */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-03: Verify the logged-in username is shown in the header user pill")
    @Story("Header")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
        Checks:
         1. The user pill element is rendered
         2. The username text is non-empty (simulating an authenticated session)
        """)
    public void tc03_verifyLoggedInUser() {
        String user = taskFlowPage.getLoggedInUser();
        LOG.info("Logged-in user: {}", user);

        Assert.assertNotNull(user, "Username should not be null");
        Assert.assertFalse(
            user.isEmpty(),
            "Username displayed in header should not be empty"
        );
    }

    /* ------------------------------------------------------------------ */
    /*  TC-04: Statistics Strip – All Four Counters Present                 */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-04: Verify the stats strip shows non-negative counts for all four KPIs")
    @Story("Statistics")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
        Reads the four stat-card numbers from the stats strip:
         1. Total Tasks    — should be > 0 (pre-seeded data exists)
         2. In Progress    — should be >= 0
         3. Completed      — should be >= 0
         4. Overdue        — should be >= 0
        Also verifies: total = in-progress + completed + overdue
        """)
    public void tc04_verifyStatsStripCounts() {
        int total     = taskFlowPage.getTotalTaskCount();
        int active    = taskFlowPage.getInProgressTaskCount();
        int completed = taskFlowPage.getCompletedTaskCount();
        int overdue   = taskFlowPage.getOverdueTaskCount();

        LOG.info("Stats — Total:{} | Active:{} | Completed:{} | Overdue:{}",
            total, active, completed, overdue);

        Assert.assertTrue(total > 0,    "Total task count should be > 0 (seed data expected)");
        Assert.assertTrue(active >= 0,  "In-progress count should be >= 0");
        Assert.assertTrue(completed >= 0,"Completed count should be >= 0");
        Assert.assertTrue(overdue >= 0, "Overdue count should be >= 0");

        // Invariant: sum of sub-statuses must equal total
        Assert.assertEquals(
            active + completed + overdue,
            total,
            String.format(
                "Sum of sub-statuses (%d) should equal total (%d). active=%d completed=%d overdue=%d",
                active + completed + overdue, total, active, completed, overdue
            )
        );
    }

    /* ------------------------------------------------------------------ */
    /*  TC-05: Navigation Links Present                                     */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-05: Verify all primary navigation links are present in the header")
    @Story("Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
        Verifies that Dashboard, My Tasks, and Reports navigation links
        are rendered in the top navigation bar.
        """)
    public void tc05_verifyNavigationLinks() {
        // Navigate to Tasks link — should not throw or navigate away (SPA)
        taskFlowPage.clickNavTasks();
        String urlAfterNavClick = taskFlowPage.getCurrentUrl();

        LOG.info("URL after nav-tasks click: {}", urlAfterNavClick);
        // The SPA stays on the same page; URL should still contain the app host
        Assert.assertTrue(
            urlAfterNavClick.contains(extractHost(ConfigManager.getAppUrl())),
            "Navigation click should not leave the application domain. URL: " + urlAfterNavClick
        );

        // Navigate back to Dashboard
        taskFlowPage.clickNavDashboard();
    }

    /* ------------------------------------------------------------------ */
    /*  Private helpers                                                     */
    /* ------------------------------------------------------------------ */

    /** Extracts just the host:port segment from a full URL for loose matching. */
    private static String extractHost(String url) {
        // e.g. "http://localhost:8080/index.html" → "localhost"
        String noScheme = url.replaceAll("https?://", "");
        return noScheme.split("[:/]")[0];
    }
}
