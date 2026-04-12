# Automation Repo – Selenium TestNG POC

This repository contains the Selenium/TestNG automation project. It is triggered by a GitHub **repository_dispatch** event from the Sample App repo. The workflow runs UI tests and generates an Allure report.

**Contents:**
- **Page Objects:** Classes under `src/main/java/com/example/pages/` encapsulate interactions with the sample web page (using the Page Object Model【19†L211-L218】 for maintainability).
- **Tests:** Located in `src/test/java/com/example/tests/`, using TestNG.  
- **pom.xml:** Maven setup with Selenium, TestNG, WebDriverManager, and Allure.  
- **GitHub Action (run-tests.yml):** Listens to `repository_dispatch` (type `run-tests`) and runs the tests.

**Payload fields used:** The dispatch payload includes `app_url` and `browser`, which are passed into the tests (via Maven system properties). For example:
- `app_url`: URL where the sample app is hosted.
- `browser`: browser to use (e.g. chrome, firefox).
- These are used in the test setup (`driver.get(System.getProperty("appUrl"))`, etc.).

**Artifacts:** After tests run, the workflow:
- Saves Surefire (TestNG) reports to an artifact `test-results`.
- Generates an Allure HTML report (`mvn allure:report`) and uploads it as `allure-report`.

See the “Setup” section below for environment variables and secrets needed.
