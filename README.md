# TaskFlow Pro — Selenium Automation Repository

> **Cross-Repository CI/CD Triggered Automation POC**  
> Selenium · Java · Maven · TestNG · Allure · GitHub Actions

---

## What This POC Demonstrates

This repository is the **automation half** of a two-repo CI/CD integration:

```
┌──────────────────────────┐    repository_dispatch     ┌────────────────────────────┐
│     app-repo             │ ─────────────────────────► │   automation-repo (this)   │
│                          │                             │                            │
│  1. Developer pushes     │                             │  4. GitHub Actions picks   │
│  2. App builds & deploys │                             │     up dispatch event      │
│  3. Post-deploy step     │                             │  5. Selenium tests run     │
│     fires dispatch ──────┤                             │  6. Allure report generated│
│                          │                             │  7. Artifacts uploaded     │
└──────────────────────────┘                             └────────────────────────────┘
```

**The key value proposition:** QA tests run automatically after every deployment — zero manual triggering, full traceability (which build, who deployed, which environment), and an Allure report always attached to the run.

---

## Repository Structure

```
automation-repo/
│
├── pom.xml                                    Maven build — Selenium, TestNG, Allure
│
├── src/
│   ├── main/java/com/qa/
│   │   ├── config/
│   │   │   └── ConfigManager.java             Reads CI-injected system properties
│   │   ├── pages/
│   │   │   ├── BasePage.java                  Abstract base — driver, waits, common actions
│   │   │   └── TaskFlowPage.java              Full page object for the SUT (40+ @Step methods)
│   │   └── utils/
│   │       ├── DriverManager.java             Thread-safe Chrome / Firefox / Edge factory
│   │       └── WaitHelper.java                Explicit wait utilities (no implicit waits)
│   │
│   └── test/
│       ├── java/com/qa/tests/
│       │   ├── BaseTest.java                  TestNG lifecycle, screenshots on fail, Allure env
│       │   ├── TaskFlowHeaderAndStatsTest.java TC-01 → TC-05  (header, stats)
│       │   ├── TaskFlowAddTaskTest.java        TC-06 → TC-11  (form, board, search, filter)
│       │   └── TaskFlowActivityFeedTest.java   TC-12          (activity feed)
│       └── resources/
│           ├── testng.xml                     Suite file — 3 test groups
│           ├── allure.properties              Results/report directories + branding
│           ├── categories.json                Allure failure categorisation rules
│           └── logback.xml                    Clean console output, suppresses Selenium noise
│
└── .github/
    └── workflows/
        └── run-automation-tests.yml           Listens for dispatch, runs tests, uploads report
```

---

## Test Coverage — 12 Test Cases

| TC | Test Class | Scenario | Severity |
|----|-----------|----------|----------|
| TC-01 | HeaderAndStats | Browser tab title contains "TaskFlow" | CRITICAL |
| TC-02 | HeaderAndStats | Header and logo are visible | NORMAL |
| TC-03 | HeaderAndStats | Logged-in username shown in header | NORMAL |
| TC-04 | HeaderAndStats | All 4 stat counters load; their sum equals total | CRITICAL |
| TC-05 | HeaderAndStats | Nav links are clickable and stay on-domain | NORMAL |
| TC-06 | AddTask | Happy path: task created → success message → stats increment | BLOCKER |
| TC-07 | AddTask | Empty title → error validation, count unchanged | CRITICAL |
| TC-08 | AddTask | Clear button empties all form fields | NORMAL |
| TC-09 | AddTask | Toggle task complete → stats update bidirectionally | CRITICAL |
| TC-10 | AddTask | Live search narrows visible task list in real-time | NORMAL |
| TC-11 | AddTask | "Completed" filter count matches stats strip | NORMAL |
| TC-12 | ActivityFeed | Activity feed shows ≥3 pre-seeded items | NORMAL |

---

## Architecture Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Driver management | ThreadLocal + DriverManager | Parallel-safe; no driver passed through constructors |
| Waits | Explicit only (WaitHelper) | Reliable; `implicit.wait=0` to avoid combined-wait anti-pattern |
| Page Objects | Extend BasePage | Shared driver, wait, and action methods without repetition |
| CI parameters | System properties via `-D` flags | Decoupled; easy to override in any environment |
| Browser download | WebDriverManager | No manual chromedriver maintenance in CI |
| Headless | `--headless=new` (Chrome) | Chromium headless v2 — more stable than the legacy flag |
| Screenshots | @AfterMethod on FAILURE only | Attached to Allure automatically via `@Attachment` |
| Allure env info | `environment.properties` at suite start | Appears in report's "Environment" block for stakeholders |

---

## How `repository_dispatch` Works

### What it is
`repository_dispatch` is a GitHub API endpoint that lets **one repo trigger a workflow in another repo**. It carries a `client_payload` JSON blob — our way of passing deployment context.

### Sending the dispatch (app-repo)
```yaml
- uses: actions/github-script@v7
  with:
    github-token: ${{ secrets.AUTOMATION_TRIGGER_TOKEN }}  # PAT with repo scope
    script: |
      await github.rest.repos.createDispatchEvent({
        owner: "your-org",
        repo:  "taskflow-automation",
        event_type: "run-automation-tests",
        client_payload: {
          env:          "staging",
          app_url:      "https://your-org.github.io/taskflow-app/",
          browser:      "chrome",
          build_number: "42",
          triggered_by: "srinivas-r"
        }
      });
```

### Receiving the dispatch (this repo)
```yaml
on:
  repository_dispatch:
    types: [run-automation-tests]

# Access payload values as:
# ${{ github.event.client_payload.app_url }}
# ${{ github.event.client_payload.browser }}
# etc.
```

### Injecting into Maven tests
```yaml
- run: |
    mvn test \
      -Dapp.url="${{ github.event.client_payload.app_url }}" \
      -Dbrowser="${{ github.event.client_payload.browser }}" \
      -Denv="${{ github.event.client_payload.env }}" \
      -Dbuild.number="${{ github.event.client_payload.build_number }}"
```

### Reading in Java
```java
// ConfigManager.java
public static String getAppUrl() {
    return System.getProperty("app.url", "http://localhost:8080");
}
```

---

## Secrets & Tokens

### What you need and where to set it

| Repo | Secret Name | Value | Where to set |
|------|-------------|-------|--------------|
| **app-repo** | `AUTOMATION_TRIGGER_TOKEN` | GitHub PAT (see below) | app-repo → Settings → Secrets |
| **app-repo** | `AUTOMATION_REPO` | `owner/taskflow-automation` | app-repo → Settings → Secrets |

> **This automation repo needs no secrets.** It only receives the dispatch and uses `GITHUB_TOKEN` (automatically available) for artifact uploads.

### Creating the Personal Access Token (PAT)

1. Log in as a GitHub user who has **write access** to this automation repo
2. Go to: `github.com` → Profile → **Settings** → **Developer settings**  
   → **Personal access tokens** → **Tokens (classic)**
3. Click **Generate new token (classic)**
4. Name: `automation-trigger-poc`
5. Expiry: 90 days
6. Scope: ✅ **`repo`** — Full control of private repositories
7. Click **Generate token** — **copy it immediately** (shown once only)
8. Go to **app-repo** → Settings → Secrets and variables → Actions  
   → **New repository secret**
   - Name: `AUTOMATION_TRIGGER_TOKEN`
   - Value: `<paste the token>`
9. Add another secret:
   - Name: `AUTOMATION_REPO`
   - Value: `your-org/taskflow-automation`

---

## Running Locally

### Prerequisites
- Java 11+
- Maven 3.8+
- Chrome (or Firefox/Edge) installed
- The sample app served at a known URL

### Step 1 — Serve the app
```bash
# From the app-repo directory
cd app-repo/src
python3 -m http.server 8080
# App now at http://localhost:8080
```

### Step 2 — Run tests (headful — see the browser)
```bash
cd automation-repo

mvn test \
  -Dapp.url=http://localhost:8080 \
  -Dbrowser=chrome \
  -Dheadless=false \
  -Denv=local \
  -Dbuild.number=local-1 \
  -Dtriggered.by=$(whoami)
```

### Step 3 — Run headless (like CI)
```bash
mvn test \
  -Dapp.url=http://localhost:8080 \
  -Dbrowser=chrome \
  -Dheadless=true
```

### Step 4 — View the Allure report
```bash
# Generate + open in browser
mvn allure:serve

# Or generate static HTML only
mvn allure:report
open target/allure-report/index.html
```

### Available System Properties

| Property | Default | Description |
|----------|---------|-------------|
| `app.url` | `http://localhost:8080` | URL of the application under test |
| `browser` | `chrome` | `chrome`, `firefox`, or `edge` |
| `headless` | `true` | `true`/`false` — headless mode |
| `env` | `staging` | Environment label in report |
| `build.number` | `local` | Build number in report |
| `triggered.by` | `manual` | Who/what triggered the run |
| `explicit.wait` | `15` | Explicit wait timeout in seconds |
| `page.load.timeout` | `30` | Page load timeout in seconds |

---

## Running via GitHub Actions (Manual)

1. Go to **Actions** → **Run Automation Tests**
2. Click **Run workflow**
3. Fill in:
   - `app_url`: the deployed application URL
   - `browser`: `chrome` / `firefox` / `edge`
   - `env`: `staging` / `uat` / `production`
4. Click **Run workflow**
5. Watch the run → check the **Summary** tab for test counts
6. Download **allure-report-build-{n}** artifact for the full interactive report

---

## Allure Report

The report is generated in CI and uploaded as an artifact (`allure-report-build-{n}`).

### What the report shows
- **Overview** — pass/fail/broken breakdown, trend chart (across runs)
- **Suites** — test grouped by class
- **Behaviors** — grouped by `@Epic → @Feature → @Story`
- **Timeline** — test execution timeline (useful for parallel runs)
- **Categories** — failure types: assertion errors, timeouts, stale elements
- **Environment** — app URL, browser, build number, who triggered
- **Screenshots** — automatically attached on failure

### Viewing locally after downloading
```bash
unzip allure-report-build-42.zip -d allure-report
# Serve it (can't open index.html directly — needs a server)
npx serve allure-report -p 9090
open http://localhost:9090
```

---

## Extending the Framework

### Adding a new page object
1. Create `src/main/java/com/qa/pages/YourPage.java` extending `BasePage`
2. Define locators as `private static final By` constants
3. Add `@Step`-annotated public methods
4. Write corresponding test class extending `BaseTest`

### Adding a new test
1. Create `src/test/java/com/qa/tests/YourTest.java` extending `BaseTest`
2. Add `@Epic`, `@Feature` class annotations
3. Add `@BeforeMethod` to open the page
4. Write `@Test` methods with `@Story`, `@Severity`, `@Description`
5. Add the class to `testng.xml` in the appropriate `<test>` block

### Running in parallel
In `testng.xml`, change:
```xml
<suite ... parallel="tests" thread-count="3">
```
Each `<test>` block gets its own thread. `ThreadLocal` in `DriverManager` keeps drivers isolated.

---

## How to Present This POC in a Meeting

### Pre-meeting checklist
- [ ] App-repo deployed to GitHub Pages and URL is working
- [ ] Secrets `AUTOMATION_TRIGGER_TOKEN` and `AUTOMATION_REPO` set in app-repo
- [ ] A prior automation run exists so you can show the Allure report
- [ ] Both repos open in browser tabs
- [ ] Allure report artifact downloaded and served locally

### Presentation script (12 minutes)

**[0:00 – 2:00] Set the scene**  
> "Right now, whenever a developer pushes code, QA has to manually kick off the test suite. We want to automate that entirely. Let me show you a working implementation."

**[2:00 – 4:00] Show the app**  
Open the deployed TaskFlow Pro URL. Walk through: header, stats, add a task, filter. Shows the audience what's being tested is a real application, not a toy page.

**[4:00 – 6:00] Trigger the flow**  
Make a small visible change in `index.html` (change "TaskFlow Pro" to "TaskFlow Pro v2"). Push to `main`. Open the app-repo Actions tab. Walk through the three jobs: Build → Deploy → Trigger Automation.

**[6:00 – 8:00] Show the dispatch**  
Expand the "Fire repository_dispatch" step. Show the payload JSON in the logs — point out `app_url`, `build_number`, `triggered_by`. Switch to the automation-repo Actions tab and show the new run that appeared automatically.

**[8:00 – 10:30] Watch tests run**  
Expand "Resolve test parameters" — show the values flowing from app-repo. Expand "Run Selenium tests via Maven" — show test output. Click the **Summary** tab while tests run (or after) — show the test count table.

**[10:30 – 12:00] Show the report**  
Open the pre-downloaded Allure report. Navigate: Overview → Behaviors → a specific test → screenshot. Point out the Environment block showing build number and who triggered.

**Closing statement:**  
> "Every push now automatically validates the deployment. The build number is tracked, the report is attached to the exact run, and nothing needs manual coordination between dev and QA."

---

## Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| Dispatch not received | PAT scope wrong or expired | Recreate PAT with `repo` scope; update secret |
| `WebDriver not initialised` | `@BeforeMethod` order issue | Ensure test class `@BeforeMethod` calls super or re-inits page object after driver |
| `TimeoutException` in CI | App URL not reachable | Check GitHub Pages deployment succeeded; verify URL in dispatch payload |
| Allure report empty | Results dir mismatch | Ensure `allure.properties` `results.directory` matches Surefire output path |
| Chrome version mismatch | WDM cache stale | Add `--no-transfer-progress` and let WDM re-resolve; or pin Chrome version |
| Tests pass locally, fail in CI | Timing differences | Increase `explicit.wait` via `-Dexplicit.wait=20`; verify `--headless=new` flag |
