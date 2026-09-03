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
| Native TestNG smoke | `src/test/resources/suites/testng.xml` | Unit checks, login, and read-only stock tests |
| TestNG unit | `src/test/resources/suites/testng-unit.xml` | Configuration and browser-state tests without opening a browser |
| TestNG mutation | `src/test/resources/suites/testng-mutation.xml` | Product lifecycle, order creation, Shopify sync, and stock sync |
| TestNG cross-browser | `src/test/resources/suites/testng-cross-browser.xml` | Native read-only tests on Chrome, Firefox, and Edge |
| Cucumber | `src/test/resources/suites/cucumber-testng.xml` | BDD feature scenarios |
| Cucumber cross-browser | `src/test/resources/suites/cross-browser.xml` | BDD scenarios on Chrome, Firefox, and Edge |

Mutation suites change application data and therefore run only when explicitly selected.

## Running tests

The default command runs the native TestNG smoke suite:

```bash
mvn clean test
```

A complete command covering the commonly used parameters is:

```bash
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/testng.xml \
  -Dtest.environment=dev \
  -Dwebdriver.browser=chrome \
  -Dwebdriver.headless=true \
  -Dexecution=local
```

Available environments are `dev`, `staging`, and `prod`. Available execution targets are `local`, `grid`, and `lambdatest`. A browser parameter inside a cross-browser XML suite takes precedence over `webdriver.browser`.

Useful TestNG commands:

```bash
# Fast tests without a browser
mvn clean test -DsuiteXmlFile=src/test/resources/suites/testng-unit.xml

# Native TestNG tests on three browsers
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/testng-cross-browser.xml \
  -Dtest.environment=staging \
  -Dwebdriver.headless=true

# Explicitly run data-changing TestNG tests
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/testng-mutation.xml \
  -Dtest.environment=dev

# Filter native TestNG methods by group
mvn clean test -Dgroups=smoke -DexcludedGroups=mutation
```

Useful Cucumber commands:

```bash
# All non-mutation BDD scenarios
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/cucumber-testng.xml \
  -Dcucumber.filter.tags='not @mutation'

# Only BDD smoke scenarios, excluding data changes
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/cucumber-testng.xml \
  -Dcucumber.filter.tags='@smoke and not @mutation'

# BDD cross-browser
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml \
  -Dcucumber.filter.tags='@smoke and not @mutation'
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
| TestNG | `target/surefire-reports/index.html` |
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

- TestNG/Cucumber suite
- `dev`, `staging`, or `prod`
- Browser and headless mode
- Local or LambdaTest execution
- Cucumber tag expression

It also runs these schedules in `Asia/Ho_Chi_Minh`:

| Local time | Scheduled run | Selection |
| --- | --- | --- |
| 00:00 every day | Nightly E2E | Cucumber suite with `@e2e` |
| 08:00 every day | Morning Smoke | Native TestNG smoke suite |

Scheduled runs use Chrome headless against `staging`. To select another scheduled environment without editing the workflow, create the repository variable `SCHEDULED_TEST_ENVIRONMENT` with `dev`, `staging`, or `prod` under **Settings → Secrets and variables → Actions → Variables**.

For non-PR runs, the workflow deploys one report portal to GitHub Pages and writes direct Allure, Extent, TestNG, and Cucumber links into the workflow **Summary**. Configure this once under **Settings → Pages → Source → GitHub Actions**.

Every run, including pull requests and failed runs, uploads `automation-reports-<run type>-<run number>` under **Actions → workflow run → Artifacts**. Nightly and morning artifacts therefore remain separate even though GitHub Pages shows the latest deployed run. Download and unzip an artifact, then serve the extracted directory when needed:

```bash
python3 -m http.server 8080
```

Open `http://localhost:8080/public/`. Pull requests intentionally do not deploy GitHub Pages, so the artifact is the report source for PR runs.

Store these repository secrets under **Settings → Secrets and variables → Actions** when credentials should not come from the checked-in environment file:

- `AUTH_USERNAME`
- `AUTH_PASSWORD`
- `LT_USERNAME` and `LT_ACCESS_KEY` for LambdaTest

The test step is allowed to finish before the workflow fails so reports are still assembled and uploaded. The final job status still reflects test failures.

## Selenium Grid and LambdaTest

```bash
# Selenium Grid
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/testng-cross-browser.xml \
  -Dexecution=grid \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dplatform=linux

# LambdaTest
export LT_USERNAME='your-user-name'
export LT_ACCESS_KEY='your-access-key'
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/testng-cross-browser.xml \
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
├── java/com/cadw/automation/base       # TestNG browser lifecycle
├── java/com/cadw/automation/tests      # Native TestNG tests
├── java/com/cadw/automation/bdd        # Cucumber runners, hooks, and steps
└── resources/
    ├── data/                           # External test data
    ├── features/                       # Gherkin specifications
    └── suites/                         # TestNG suite XML files
```
