# CADW Test Automation

Java 17, Selenium WebDriver, TestNG, and Cucumber automation for the CADW back-office application. The framework supports environment-based configuration, reusable authenticated browser state, local/Grid/LambdaTest execution, and Allure, Extent, TestNG, and Cucumber reports.

## Prerequisites

- JDK 17 or newer
- Maven 3.9+
- Chrome, Firefox, or Edge for local UI tests

Selenium Manager resolves the matching local driver automatically.

## Test suites

| Suite | File | Purpose |
| --- | --- | --- |
| Cucumber single-browser | `src/test/resources/suites/cucumber-testng.xml` | Runs selected Cucumber tags on one browser |
| Cucumber cross-browser | `src/test/resources/suites/cross-browser.xml` | Runs selected Cucumber tags on Chrome, Firefox, and Edge |

TestNG remains the Cucumber JVM runner, but functional tests are selected and executed exclusively as Cucumber scenarios.

Cucumber scenarios run with up to four parallel workers in the single-browser suite. Scenarios tagged `@serial` or `@mutation` acquire an exclusive execution lock, so data-changing flows never overlap with read-only scenarios. The cross-browser suite runs its three browsers in parallel and allows up to two scenario workers per browser.

| Scope | Cucumber expression | Coverage |
| --- | --- | --- |
| Smoke | `@smoke and not @mutation` | Fast critical paths without changing application data |
| E2E | `@e2e` | Complete end-to-end Stock flows, including product lifecycle |
| Regression | `@regression` | Every active regression scenario, including mutation and synchronization |

## Running tests

The default command runs the safe Cucumber smoke scope:

```bash
mvn clean test
```

A complete command covering the commonly used parameters is:

```bash
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/cucumber-testng.xml \
  -Dcucumber.filter.tags='@smoke and not @mutation' \
  -Dtest.environment=dev \
  -Dwebdriver.browser=chrome \
  -Dwebdriver.headless=true \
  -Dexecution=local
```

Available environments are `dev`, `staging`, and `prod`. Available execution targets are `local`, `grid`, and `lambdatest`. A browser parameter inside a cross-browser XML suite takes precedence over `webdriver.browser`.

Run each Cucumber scope locally:

```bash
# Smoke
mvn clean test -Dcucumber.filter.tags='@smoke and not @mutation'

# E2E (includes mutation)
mvn clean test -Dcucumber.filter.tags='@e2e'

# Full regression (includes mutation)
mvn clean test -Dcucumber.filter.tags='@regression'

# Cross-browser regression
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml \
  -Dcucumber.filter.tags='@regression'
```

Every key from the selected `.config` file can be overridden by a JVM property or its uppercase environment-variable form. For example, `auth.username` maps to `AUTH_USERNAME`, and `webdriver.headless` maps to `WEBDRIVER_HEADLESS`.

## Reusing login state

Authenticated tests restore and persist cookies, `localStorage`, and `sessionStorage`. State is isolated by environment, browser, and account under `.browser-state/`, expires after 24 hours by default, and is excluded from Git because it may contain authentication tokens.

```properties
auth.state.enabled=true
auth.state.directory=.browser-state
auth.state.max-age.hours=24
```

Delete the matching file under `.browser-state/`, or call `DriverFactory.clearBrowserState()`, to force a fresh login. Login tests always clear the active browser session first so both successful and invalid-login cases remain independent.

## Reading reports locally

After a test run, the framework produces:

| Report | Local entry point |
| --- | --- |
| Extent | `target/extent-report/index.html` |
| Cucumber TestNG runner | `target/surefire-reports/index.html` |
| Cucumber | `target/cucumber-report/cucumber.html` (Cucumber suites only) |
| Allure raw results | `target/allure-results/` |
| Logs | `target/logs/automation.log` |

Extent, TestNG, and Cucumber are static HTML and can be opened directly. On macOS:

```bash
open target/extent-report/index.html
open target/surefire-reports/index.html
open target/cucumber-report/cucumber.html
```

For Allure, generate or serve the site:

```bash
mvn allure:report
mvn allure:serve
```

The generated Allure entry point is `target/site/allure-maven-plugin/index.html`. If a browser blocks assets opened with `file://`, serve all reports through HTTP:

```bash
python3 -m http.server 8080 --directory target
```

Then open `http://localhost:8080/extent-report/`, `http://localhost:8080/surefire-reports/`, or `http://localhost:8080/cucumber-report/cucumber.html`.

## Reports in GitHub Actions

The **Test automation** workflow supports manual selection of:

- Single-browser or cross-browser Cucumber suite
- `smoke`, `e2e`, or `regression` scope
- `dev`, `staging`, or `prod`
- Browser and headless mode
- Local or LambdaTest execution

It also runs these schedules in `Asia/Ho_Chi_Minh`:

| Local time | Scheduled run | Selection |
| --- | --- | --- |
| 00:00 every day | Nightly E2E | Cucumber suite with `@e2e` |
| 08:00 every day | Morning Smoke | Cucumber suite with `@smoke and not @mutation` |

Scheduled runs use Chrome headless against `staging`. To select another scheduled environment without editing the workflow, create the repository variable `SCHEDULED_TEST_ENVIRONMENT` with `dev`, `staging`, or `prod` under **Settings → Secrets and variables → Actions → Variables**.

Every workflow run uploads the report bundle as an Actions artifact, independently of GitHub Pages. To additionally publish the report portal, first select **Settings → Pages → Source → GitHub Actions**, then create the repository variable `PUBLISH_GITHUB_PAGES=true` under **Settings → Secrets and variables → Actions → Variables**. Pages deployment stays disabled when this variable is absent or false, so a repository without Pages enabled cannot make the test workflow fail.

Every run, including pull requests and failed runs, uploads `automation-reports-<run type>-<run number>` under **Actions → workflow run → Artifacts**. Nightly and morning artifacts therefore remain separate even though GitHub Pages shows the latest deployed run. Download and unzip an artifact, then serve the extracted directory when needed:

```bash
python3 -m http.server 8080
```

Open `http://localhost:8080/public/`. Pull requests intentionally do not deploy GitHub Pages, so the artifact is the report source for PR runs.

Store these repository secrets under **Settings → Secrets and variables → Actions** when credentials should not come from the checked-in environment file:

- `TEST_CONFIG_DEV` containing the complete `dev.config` content
- `TEST_CONFIG_STAGING` containing the complete `staging.config` content
- `TEST_CONFIG_PROD` containing the complete `prod.config` content
- `AUTH_USERNAME`
- `AUTH_PASSWORD`
- `LT_USERNAME` and `LT_ACCESS_KEY` for LambdaTest

The `.config` files are intentionally ignored by Git. GitHub Actions recreates all three files from these secrets before Maven runs. They can be uploaded without printing their content by using GitHub CLI:

```bash
gh secret set TEST_CONFIG_DEV < src/main/java/com/cadw/automation/config/dev.config
gh secret set TEST_CONFIG_STAGING < src/main/java/com/cadw/automation/config/staging.config
gh secret set TEST_CONFIG_PROD < src/main/java/com/cadw/automation/config/prod.config
```

The test step is allowed to finish before the workflow fails so reports are still assembled and uploaded. The final job status still reflects test failures.

## Selenium Grid and LambdaTest

```bash
# Selenium Grid
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml \
  -Dcucumber.filter.tags='@smoke and not @mutation' \
  -Dexecution=grid \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dplatform=linux

# LambdaTest
export LT_USERNAME='your-user-name'
export LT_ACCESS_KEY='your-access-key'
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml \
  -Dcucumber.filter.tags='@smoke and not @mutation' \
  -Dexecution=lambdatest \
  -Dlt.platform='Windows 11' \
  -Dlt.browser.version=latest \
  -Dlt.build='release-42'
```

## Project layout

```text
src/main/java/com/cadw/automation/
├── config/       # EnvironmentConfig and environment files
├── driver/       # Thread-local local/Grid/LambdaTest drivers
├── pages/        # Page Objects
├── reporting/    # Extent report lifecycle
└── state/        # Cookies and Web Storage persistence

src/test/
├── java/com/cadw/automation/base       # Shared browser lifecycle
├── java/com/cadw/automation/tests      # Native support tests, excluded from functional CI
├── java/com/cadw/automation/bdd        # Cucumber TestNG runners, hooks, and steps
└── resources/
    ├── data/                           # External test data
    ├── features/                       # Gherkin specifications
    └── suites/                         # TestNG suite XML files
```
