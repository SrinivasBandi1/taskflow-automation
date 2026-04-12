package com.qa.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TaskFlowPage
 *
 * Page Object for the TaskFlow Pro single-page application.
 * Covers all interactive regions:
 *  - Header / navigation
 *  - Stats strip (totals, in-progress, completed, overdue)
 *  - Add Task form (inputs, selects, submit, clear, messages)
 *  - Filter / search bar
 *  - Task list items
 *  - Activity feed
 *
 * All public methods carry Allure @Step annotations so every action
 * appears in the test report automatically.
 */
public class TaskFlowPage extends BasePage {

    private static final Logger LOG = LoggerFactory.getLogger(TaskFlowPage.class);

    /* ------------------------------------------------------------------ */
    /*  Locators – using IDs wherever possible for stability               */
    /* ------------------------------------------------------------------ */

    // Header
    private static final By SITE_HEADER     = By.id("site-header");
    private static final By LOGO_TEXT       = By.cssSelector(".logo-text");
    private static final By NAV_DASHBOARD   = By.id("nav-dashboard");
    private static final By NAV_TASKS       = By.id("nav-tasks");
    private static final By NAV_REPORTS     = By.id("nav-reports");
    private static final By LOGGED_IN_USER  = By.id("logged-in-user");

    // Stats strip
    private static final By STAT_TOTAL      = By.id("stat-total-count");
    private static final By STAT_ACTIVE     = By.id("stat-active-count");
    private static final By STAT_COMPLETED  = By.id("stat-completed-count");
    private static final By STAT_OVERDUE    = By.id("stat-overdue-count");

    // Add Task form
    private static final By TASK_TITLE_INPUT    = By.id("task-title");
    private static final By TASK_ASSIGNEE_INPUT = By.id("task-assignee");
    private static final By TASK_PRIORITY_SELECT= By.id("task-priority");
    private static final By TASK_DUE_DATE_INPUT = By.id("task-due-date");
    private static final By TASK_DESC_TEXTAREA  = By.id("task-description");
    private static final By BTN_ADD_TASK        = By.id("btn-add-task");
    private static final By BTN_CLEAR_FORM      = By.id("btn-clear-form");
    private static final By FORM_MESSAGE        = By.id("form-message");

    // Filter bar
    private static final By SEARCH_INPUT       = By.id("search-input");
    private static final By FILTER_ALL         = By.id("filter-all");
    private static final By FILTER_INPROGRESS  = By.id("filter-inprogress");
    private static final By FILTER_COMPLETED   = By.id("filter-completed");
    private static final By FILTER_OVERDUE     = By.id("filter-overdue");

    // Task list
    private static final By TASK_LIST          = By.id("task-list");
    private static final By TASK_ITEMS         = By.cssSelector(".task-item");
    private static final By EMPTY_STATE        = By.id("empty-state");

    // Activity feed
    private static final By ACTIVITY_FEED      = By.id("activity-feed");
    private static final By ACTIVITY_ITEMS     = By.cssSelector(".activity-item");

    /* ================================================================== */
    /*  Page-level actions                                                 */
    /* ================================================================== */

    @Step("Wait for TaskFlow Pro page to load completely")
    public TaskFlowPage waitForPageToLoad() {
        waitForPageLoad();
        wait.waitForVisibility(SITE_HEADER);
        LOG.info("Page loaded: {}", driver.getTitle());
        return this;
    }

    /* ================================================================== */
    /*  Header                                                             */
    /* ================================================================== */

    @Step("Get page header text")
    public String getLogoText() {
        return getText(LOGO_TEXT);
    }

    @Step("Check header is displayed")
    public boolean isHeaderDisplayed() {
        return isDisplayed(SITE_HEADER);
    }

    @Step("Get logged-in username from header")
    public String getLoggedInUser() {
        return getText(LOGGED_IN_USER);
    }

    @Step("Click Navigation: Dashboard")
    public TaskFlowPage clickNavDashboard() {
        click(NAV_DASHBOARD);
        return this;
    }

    @Step("Click Navigation: My Tasks")
    public TaskFlowPage clickNavTasks() {
        click(NAV_TASKS);
        return this;
    }

    /* ================================================================== */
    /*  Stats Strip                                                        */
    /* ================================================================== */

    @Step("Get total task count from stats strip")
    public int getTotalTaskCount() {
        return Integer.parseInt(getText(STAT_TOTAL).trim());
    }

    @Step("Get in-progress task count from stats strip")
    public int getInProgressTaskCount() {
        return Integer.parseInt(getText(STAT_ACTIVE).trim());
    }

    @Step("Get completed task count from stats strip")
    public int getCompletedTaskCount() {
        return Integer.parseInt(getText(STAT_COMPLETED).trim());
    }

    @Step("Get overdue task count from stats strip")
    public int getOverdueTaskCount() {
        return Integer.parseInt(getText(STAT_OVERDUE).trim());
    }

    /* ================================================================== */
    /*  Add Task Form                                                      */
    /* ================================================================== */

    @Step("Enter task title: {title}")
    public TaskFlowPage enterTaskTitle(String title) {
        typeInto(TASK_TITLE_INPUT, title);
        return this;
    }

    @Step("Enter assignee: {assignee}")
    public TaskFlowPage enterAssignee(String assignee) {
        typeInto(TASK_ASSIGNEE_INPUT, assignee);
        return this;
    }

    @Step("Select priority: {priority}")
    public TaskFlowPage selectPriority(String priority) {
        selectByVisibleText(TASK_PRIORITY_SELECT, priority);
        return this;
    }

    @Step("Enter due date: {date} (format yyyy-MM-dd)")
    public TaskFlowPage enterDueDate(String date) {
        // Direct value injection via JS is more reliable for date inputs
        WebElement dateInput = wait.waitForVisibility(TASK_DUE_DATE_INPUT);
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", dateInput, date);
        return this;
    }

    @Step("Enter task description: {description}")
    public TaskFlowPage enterDescription(String description) {
        typeInto(TASK_DESC_TEXTAREA, description);
        return this;
    }

    @Step("Click 'Add Task' button")
    public TaskFlowPage clickAddTask() {
        click(BTN_ADD_TASK);
        return this;
    }

    @Step("Click 'Clear' form button")
    public TaskFlowPage clickClearForm() {
        click(BTN_CLEAR_FORM);
        return this;
    }

    /**
     * Complete task-creation form and submit in one step.
     * Chains: title → assignee → priority → submit
     */
    @Step("Add new task: title='{title}', assignee='{assignee}', priority='{priority}'")
    public TaskFlowPage addNewTask(String title, String assignee, String priority) {
        enterTaskTitle(title);
        enterAssignee(assignee);
        selectPriority(priority);
        clickAddTask();
        return this;
    }

    /* ------------------------------------------------------------------ */
    /*  Form messages                                                       */
    /* ------------------------------------------------------------------ */

    @Step("Wait for and get the form feedback message")
    public String getFormMessage() {
        wait.waitForVisibility(FORM_MESSAGE);
        return getText(FORM_MESSAGE);
    }

    @Step("Check form message is visible")
    public boolean isFormMessageVisible() {
        return isDisplayed(FORM_MESSAGE) &&
               !getCssClass(FORM_MESSAGE).contains("hidden");
    }

    @Step("Check form message is a success message")
    public boolean isFormMessageSuccess() {
        String cssClass = getCssClass(FORM_MESSAGE);
        return cssClass.contains("success");
    }

    @Step("Check form message is an error message")
    public boolean isFormMessageError() {
        String cssClass = getCssClass(FORM_MESSAGE);
        return cssClass.contains("error");
    }

    /* ================================================================== */
    /*  Filter & Search                                                    */
    /* ================================================================== */

    @Step("Search for tasks with keyword: '{keyword}'")
    public TaskFlowPage searchTasks(String keyword) {
        typeInto(SEARCH_INPUT, keyword);
        return this;
    }

    @Step("Clear search box")
    public TaskFlowPage clearSearch() {
        typeInto(SEARCH_INPUT, "");
        return this;
    }

    @Step("Click filter: All tasks")
    public TaskFlowPage filterAll() {
        click(FILTER_ALL);
        return this;
    }

    @Step("Click filter: In Progress")
    public TaskFlowPage filterInProgress() {
        click(FILTER_INPROGRESS);
        return this;
    }

    @Step("Click filter: Completed")
    public TaskFlowPage filterCompleted() {
        click(FILTER_COMPLETED);
        return this;
    }

    @Step("Click filter: Overdue")
    public TaskFlowPage filterOverdue() {
        click(FILTER_OVERDUE);
        return this;
    }

    /* ================================================================== */
    /*  Task List                                                          */
    /* ================================================================== */

    @Step("Get visible task count in task board")
    public int getVisibleTaskCount() {
        return driver.findElements(TASK_ITEMS).size();
    }

    @Step("Check if task with title '{taskTitle}' is visible in the list")
    public boolean isTaskVisible(String taskTitle) {
        List<WebElement> items = driver.findElements(TASK_ITEMS);
        return items.stream().anyMatch(item ->
            item.getText().contains(taskTitle));
    }

    @Step("Check if empty-state message is shown")
    public boolean isEmptyStateVisible() {
        return isDisplayed(EMPTY_STATE) &&
               !getCssClass(EMPTY_STATE).contains("hidden");
    }

    /**
     * Clicks the delete button for a specific task identified by its DOM id.
     * Format: btn-delete-{taskId}
     */
    @Step("Delete task with id: {taskId}")
    public TaskFlowPage deleteTask(int taskId) {
        By deleteBtn = By.id("btn-delete-" + taskId);
        wait.waitForClickability(deleteBtn).click();
        LOG.info("Deleted task id={}", taskId);
        return this;
    }

    /**
     * Clicks the checkbox to toggle a task's completed state.
     * Format: task-check-{taskId}
     */
    @Step("Toggle complete status of task id: {taskId}")
    public TaskFlowPage toggleTaskComplete(int taskId) {
        By checkBox = By.id("task-check-" + taskId);
        wait.waitForClickability(checkBox).click();
        LOG.info("Toggled task id={}", taskId);
        return this;
    }

    /** Returns true if the task item currently shows the completed style (line-through). */
    @Step("Check if task id {taskId} is marked as completed")
    public boolean isTaskMarkedCompleted(int taskId) {
        By titleText = By.id("task-title-text-" + taskId);
        String cssClass = getCssClass(titleText);
        return cssClass.contains("completed-text");
    }

    /* ================================================================== */
    /*  Activity Feed                                                      */
    /* ================================================================== */

    @Step("Check if activity feed section is displayed")
    public boolean isActivityFeedDisplayed() {
        return isDisplayed(ACTIVITY_FEED);
    }

    @Step("Get count of activity items in the feed")
    public int getActivityItemCount() {
        return driver.findElements(ACTIVITY_ITEMS).size();
    }

    @Step("Get text of the first activity item")
    public String getFirstActivityItemText() {
        List<WebElement> items = driver.findElements(ACTIVITY_ITEMS);
        if (items.isEmpty()) return "";
        return items.get(0).getText().trim();
    }
}
