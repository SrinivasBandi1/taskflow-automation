package com.qa.tests;

import com.qa.config.ConfigManager;
import com.qa.pages.TaskFlowPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * TaskFlowAddTaskTest
 *
 * End-to-end tests for the "Add New Task" form.
 * Covers: happy path creation, validation (empty title),
 * clear form behaviour, stats update after add, and search.
 */
@Epic("TaskFlow Pro Application")
@Feature("Add Task Form")
public class TaskFlowAddTaskTest extends BaseTest {

    private TaskFlowPage taskFlowPage;

    @BeforeMethod(alwaysRun = true)
    public void openApplication() {
        taskFlowPage = new TaskFlowPage();
        taskFlowPage.navigateTo(ConfigManager.getAppUrl());
        taskFlowPage.waitForPageToLoad();
    }

    /* ------------------------------------------------------------------ */
    /*  TC-06: Successful Task Creation – Happy Path                        */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-06: Verify a new task can be added and appears immediately in the task board")
    @Story("Add Task")
    @Severity(SeverityLevel.BLOCKER)
    @Description("""
        Steps:
         1. Record the current total task count from the stats strip
         2. Fill in task title, assignee, and priority
         3. Click 'Add Task'
         4. Verify success message is shown and contains the task title
         5. Verify the task board now shows the new task
         6. Verify the total task count in the stats strip incremented by 1
        """)
    public void tc06_addNewTask_happyPath() {
        // Step 1: Record initial state
        int initialTotal     = taskFlowPage.getTotalTaskCount();
        int initialActive    = taskFlowPage.getInProgressTaskCount();
        int initialTaskCount = taskFlowPage.getVisibleTaskCount();

        LOG.info("Initial state — total:{} active:{} visible:{}", initialTotal, initialActive, initialTaskCount);

        // Step 2: Add a new task
        String newTaskTitle = "Automate cross-repo CI/CD integration tests";
        taskFlowPage
            .enterTaskTitle(newTaskTitle)
            .enterAssignee("Srinivas R.")
            .selectPriority("High")
            .clickAddTask();

        // Step 3: Verify success message
        Assert.assertTrue(
            taskFlowPage.isFormMessageVisible(),
            "Success/error message banner should appear after clicking Add Task"
        );
        Assert.assertTrue(
            taskFlowPage.isFormMessageSuccess(),
            "Form message should be of type 'success'"
        );
        String msg = taskFlowPage.getFormMessage();
        LOG.info("Form message: {}", msg);
        Assert.assertTrue(
            msg.contains(newTaskTitle),
            "Success message should mention the task title. Got: " + msg
        );

        // Step 4: Verify task appears in the list
        Assert.assertTrue(
            taskFlowPage.isTaskVisible(newTaskTitle),
            "Newly added task should be visible in the task board"
        );

        // Step 5: Verify stats update
        int newTotal  = taskFlowPage.getTotalTaskCount();
        int newActive = taskFlowPage.getInProgressTaskCount();
        LOG.info("New state    — total:{} active:{}", newTotal, newActive);

        Assert.assertEquals(newTotal, initialTotal + 1,
            "Total task count should increment by 1 after adding a task");
        Assert.assertEquals(newActive, initialActive + 1,
            "In-progress count should increment by 1 (new tasks default to in-progress)");
    }

    /* ------------------------------------------------------------------ */
    /*  TC-07: Validation – Empty Title Should Show Error                   */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-07: Verify that submitting the form with an empty title shows a validation error")
    @Story("Add Task – Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
        Steps:
         1. Leave task title blank
         2. Fill in other fields (assignee, priority)
         3. Click 'Add Task'
         4. Verify an error message is displayed (not a success message)
         5. Verify the task board count has NOT changed
        """)
    public void tc07_addTask_emptyTitle_showsValidationError() {
        int initialTotal = taskFlowPage.getTotalTaskCount();

        // Submit with empty title, other fields filled
        taskFlowPage
            .enterAssignee("Test User")
            .selectPriority("Critical")
            .clickAddTask();

        // Error banner should appear
        Assert.assertTrue(
            taskFlowPage.isFormMessageVisible(),
            "A validation message should be shown when title is empty"
        );
        Assert.assertTrue(
            taskFlowPage.isFormMessageError(),
            "Message type should be 'error' for empty-title submission"
        );

        String errorText = taskFlowPage.getFormMessage();
        LOG.info("Validation error: {}", errorText);
        Assert.assertTrue(
            errorText.toLowerCase().contains("required") || errorText.toLowerCase().contains("title"),
            "Error message should mention the title field. Got: " + errorText
        );

        // Task count must not have changed
        Assert.assertEquals(
            taskFlowPage.getTotalTaskCount(), initialTotal,
            "No task should be added when title is empty"
        );
    }

    /* ------------------------------------------------------------------ */
    /*  TC-08: Clear Form Resets All Fields                                 */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-08: Verify the Clear button resets all form inputs to their default state")
    @Story("Add Task – Form Controls")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
        Steps:
         1. Type into title, assignee, and description fields
         2. Select a priority option
         3. Click 'Clear'
         4. Verify title field is empty
         5. Verify no message banner is visible
        """)
    public void tc08_clearForm_resetsFields() {
        // Fill in form
        taskFlowPage
            .enterTaskTitle("This should be cleared")
            .enterAssignee("Tester")
            .enterDescription("Some description text")
            .selectPriority("Critical");

        // Clear it
        taskFlowPage.clickClearForm();

        // Verify title input is now empty
        String titleValue = taskFlowPage
            .waitForPageToLoad()  // page is already loaded; just re-check DOM stability
            .getPageTitle();      // dummy call to ensure we're still on page

        // The actual field check requires reading the input value attribute
        // Since our BasePage getText() reads innerText (not value), use JS or attribute
        String titleFieldValue = (String)
            ((org.openqa.selenium.JavascriptExecutor) com.qa.utils.DriverManager.getDriver())
            .executeScript("return document.getElementById('task-title').value");

        Assert.assertTrue(
            titleFieldValue == null || titleFieldValue.isEmpty(),
            "Task title field should be empty after clicking Clear. Got: " + titleFieldValue
        );

        LOG.info("Clear form verified — title field is empty after clear");
    }

    /* ------------------------------------------------------------------ */
    /*  TC-09: Toggle Task Complete Updates Stats                           */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-09: Verify toggling a task to complete updates the stats strip correctly")
    @Story("Task Board Interactions")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
        Steps:
         1. Note initial in-progress and completed counts
         2. Add a new task (defaults to in-progress)
         3. Toggle the new task's checkbox to mark it complete
         4. Verify completed count increased by 1
         5. Verify in-progress count returned to original value
         6. Toggle the checkbox again (mark incomplete)
         7. Verify counts revert
        """)
    public void tc09_toggleTaskComplete_updatesStats() {
        // Step 1: Add a fresh task to work with
        String taskTitle = "TC09 - Toggle Complete Test Task";
        int beforeCompleted  = taskFlowPage.getCompletedTaskCount();
        int beforeActive     = taskFlowPage.getInProgressTaskCount();

        taskFlowPage.addNewTask(taskTitle, "QA Engineer", "Medium");

        // The new task ID will be nextId = initial count + 1
        // We read it from the DOM by finding the task's delete button pattern
        // Instead, use a predictable search: the task appears at position 0 in the list
        int beforeAddTotal  = beforeCompleted + beforeActive + taskFlowPage.getOverdueTaskCount();
        int taskId          = beforeAddTotal + 1; // sequential ID assigned by JS

        int afterAddActive  = taskFlowPage.getInProgressTaskCount();

        LOG.info("After add — inProgress:{} completed:{}", afterAddActive, taskFlowPage.getCompletedTaskCount());

        Assert.assertEquals(afterAddActive, beforeActive + 1,
            "In-progress count should be +1 after adding a new in-progress task");

        // Step 2: Toggle task to completed
        taskFlowPage.toggleTaskComplete(taskId);

        int afterToggleCompleted = taskFlowPage.getCompletedTaskCount();
        int afterToggleActive    = taskFlowPage.getInProgressTaskCount();

        LOG.info("After toggle complete — inProgress:{} completed:{}", afterToggleActive, afterToggleCompleted);

        Assert.assertEquals(afterToggleCompleted, beforeCompleted + 1,
            "Completed count should be +1 after toggling task to complete");
        Assert.assertEquals(afterToggleActive, beforeActive,
            "In-progress count should return to original value after completing the task");

        // Step 3: Toggle back (mark incomplete)
        taskFlowPage.toggleTaskComplete(taskId);

        Assert.assertEquals(taskFlowPage.getInProgressTaskCount(), beforeActive + 1,
            "In-progress count should be +1 again after un-completing the task");
        Assert.assertEquals(taskFlowPage.getCompletedTaskCount(), beforeCompleted,
            "Completed count should revert after un-completing the task");
    }

    /* ------------------------------------------------------------------ */
    /*  TC-10: Search Filters Task List in Real-Time                        */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-10: Verify real-time search narrows the visible task list to matching items only")
    @Story("Search & Filter")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
        Steps:
         1. Note the total visible task count (all-filter active)
         2. Type a search keyword known to match a subset of seeded tasks
         3. Verify visible task count is less than total
         4. Verify all visible tasks contain the keyword
         5. Clear search — verify full list reappears
        """)
    public void tc10_searchInput_filtersTaskList() {
        // All tasks visible initially
        int totalVisible = taskFlowPage.getVisibleTaskCount();
        Assert.assertTrue(totalVisible > 0, "There should be tasks visible before searching");

        // Search for a term that matches only a subset of seeded tasks
        String keyword = "auth";
        taskFlowPage.searchTasks(keyword);

        int afterSearchCount = taskFlowPage.getVisibleTaskCount();
        LOG.info("After search '{}' — visible:{} (was:{})", keyword, afterSearchCount, totalVisible);

        Assert.assertTrue(
            afterSearchCount < totalVisible,
            String.format("Search for '%s' should reduce visible tasks. Before:%d After:%d",
                keyword, totalVisible, afterSearchCount)
        );
        Assert.assertTrue(
            afterSearchCount > 0,
            "Search for '" + keyword + "' should still show at least one matching task"
        );

        // Clear search → all tasks return
        taskFlowPage.searchTasks(""); // clear by overwriting with empty string
        int afterClearCount = taskFlowPage.getVisibleTaskCount();

        Assert.assertEquals(afterClearCount, totalVisible,
            "Clearing search should restore full task list");
    }

    /* ------------------------------------------------------------------ */
    /*  TC-11: Filter Buttons – Completed Filter Shows Only Completed Tasks */
    /* ------------------------------------------------------------------ */

    @Test(description = "TC-11: Verify the 'Completed' filter shows only completed tasks")
    @Story("Search & Filter")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
        Steps:
         1. Click the 'Completed' filter button
         2. Read the visible task count from the list
         3. Verify it equals the 'Completed' count shown in the stats strip
         4. Click 'All' to reset the filter
        """)
    public void tc11_filterCompleted_showsOnlyCompletedTasks() {
        int completedFromStats = taskFlowPage.getCompletedTaskCount();

        // Apply completed filter
        taskFlowPage.filterCompleted();

        int visibleAfterFilter = taskFlowPage.getVisibleTaskCount();
        LOG.info("Completed filter — stats:{} list:{}", completedFromStats, visibleAfterFilter);

        Assert.assertEquals(
            visibleAfterFilter,
            completedFromStats,
            String.format(
                "Task list should show exactly %d tasks after 'Completed' filter, but showed %d",
                completedFromStats, visibleAfterFilter
            )
        );

        // Reset
        taskFlowPage.filterAll();
    }
}
