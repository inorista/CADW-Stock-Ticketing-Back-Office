# Selenium Test Automation Framework

A Java 17, Selenium WebDriver, TestNG, and Cucumber framework for scalable local, Selenium Grid, and LambdaTest execution. It uses Page Objects, thread-local browser sessions, external JSON test data, SLF4J/Logback logging, and both Allure and Extent reports.

## What is included

- Chrome, Firefox, and Edge through Selenium Manager
- Local, Selenium Grid, and LambdaTest execution targets
- Thread-safe parallel TestNG and parallel Cucumber execution; cross-browser sessions run concurrently while Google searches remain sequential per browser to reduce rate limiting
- Page Object Model with explicit waits and no shared mutable drivers
- TestNG tests plus Cucumber/Gherkin examples
- JSON test data kept outside test logic
- Screenshots on failures in Allure and Extent reports
- HTTP status validation for all unique links on the Google home page
- GitHub Actions cross-browser execution and GitHub Pages report publishing

The search suite contains three passing examples and one intentionally failing example, grouped as `intentional-failure`. This is deliberate so both successful and failed states appear in the reports.

## Prerequisites

- JDK 17 or newer
- Maven 3.9+
- At least one supported browser installed for local execution

Selenium Manager resolves compatible local driver binaries automatically.

## Quick start

Run the default Chrome suite in parallel, including the intentional failure:

```bash
mvn clean test
```

Run a green suite without the demonstration failure:

```bash
mvn clean test -DexcludedGroups=intentional-failure
```

Run all three browsers in parallel:

```bash
mvn clean test -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml
```

Run Cucumber scenarios in parallel:

```bash
mvn clean test -DsuiteXmlFile=src/test/resources/suites/cucumber-testng.xml
```

Configuration is loaded from `dev.config`, `staging.config`, or `prod.config`. Select the environment with `-Dtest.environment=staging` or `TEST_ENVIRONMENT=staging`; it defaults to `dev`.

Override any setting using a JVM property or an uppercase environment variable. For example, `base.url` maps to `BASE_URL`, and `lt.access.key` maps to `LT_ACCESS_KEY`.

```bash
mvn test -Dwebdriver.browser=firefox -Dwebdriver.headless=false
```

## Reusing login state

The framework automatically restores cookies, `localStorage`, and `sessionStorage` when a browser starts, then saves them before the browser closes. State is isolated by environment, browser, and account profile under `.browser-state/`, expires after 24 hours by default, and is excluded from Git because it can contain authentication tokens.

After a successful login, save immediately when later scenarios may start in parallel:

```java
DriverFactory.saveBrowserState();
```

Use `DriverFactory.clearBrowserState()` to force a fresh login. Configure the behavior in the selected environment file:

```properties
auth.state.enabled=true
auth.state.directory=.browser-state
auth.state.max-age.hours=24
```

## Reports and logs

After a run:

- Extent: `target/extent-report/index.html`
- Allure results: `target/allure-results`
- Cucumber HTML: `target/cucumber-report/cucumber.html`
- Logs: `target/logs/automation.log`

Generate the Allure HTML site with:

```bash
mvn allure:report
```

The generated Allure entry point is `target/site/allure-maven-plugin/index.html`. To start a temporary local report server, use `mvn allure:serve`.

## Selenium Grid

Start a compatible Grid, then override the execution target and URL:

```bash
mvn test \
  -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml \
  -Dexecution=grid \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dplatform=linux
```

Parallelism is controlled in the TestNG suite XML files. Every invocation receives an isolated driver from `DriverSession`.

## LambdaTest

Do not commit credentials. Export them, then select the LambdaTest execution target:

```bash
export LT_USERNAME='your-user-name'
export LT_ACCESS_KEY='your-access-key'

mvn test \
  -DsuiteXmlFile=src/test/resources/suites/cross-browser.xml \
  -Dexecution=lambdatest \
  -Dlt.platform='Windows 11' \
  -Dlt.browser.version=latest \
  -Dlt.build='release-42'
```

The framework sends W3C `LT:Options`, names each remote session, and updates its pass/fail status. Optional video, network, console, and tunnel settings live in the selected environment config file.

## GitHub Actions and live report links

The workflow at `.github/workflows/test-automation.yml` runs Chrome, Firefox, and Edge on `windows-latest`, uploads raw report artifacts, and publishes a small report portal to GitHub Pages. It writes direct Allure and Extent links to the workflow summary even when a demonstration test fails.

One-time repository setup:

1. Open **Settings → Pages** and select **GitHub Actions** as the source.
2. For LambdaTest workflow dispatches, add `LT_USERNAME` and `LT_ACCESS_KEY` under **Settings → Secrets and variables → Actions**.
3. Push to `main`/`master`, or run **Test automation** manually. Choose `lambdatest` to use the cloud grid.

The test command is allowed to finish with failures so reports can still be generated and deployed. The workflow restores the failing status after publishing, so genuine failures remain visible.

## Project layout

```text
src/main/java/com/example/automation/
├── config/       # Environment-based property loading
├── driver/       # Thread-local local/Grid/LambdaTest driver creation
├── pages/        # Reusable Page Objects
├── reporting/    # Extent report lifecycle
└── utils/        # Link checking and screenshots

src/test/
├── java/.../base       # TestNG BaseTest lifecycle
├── java/.../tests      # Google sample tests
├── java/.../bdd        # Cucumber runner, hooks, and steps
└── resources/
    ├── config/         # Runtime configuration
    ├── data/           # JSON test data
    ├── features/       # Gherkin specifications
    └── suites/         # TestNG execution suites
```

## Adding tests

1. Add user interactions and state queries to a Page Object.
2. Put scenario values in `src/test/resources/data`, not in test logic.
3. Extend `BaseTest` for TestNG tests, or add Gherkin steps under the Cucumber glue package.
4. Keep assertions in tests/steps and browser mechanics in pages or utilities.

Google can vary markup, consent prompts, and anti-automation behavior by region. The sample uses English (`hl=en`), resilient search-box selectors, and an optional consent handler. If Google returns its unusual-traffic CAPTCHA, passing search examples are marked skipped with a clear reason; the deliberate negative example still fails. Production suites should use an application and environment you control.
