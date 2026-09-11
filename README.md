# API Testing with REST Assured

Automated test suite for the [JSONPlaceholder](https://jsonplaceholder.typicode.com/) fake
REST API, built with REST Assured, JUnit 5, and Allure reporting, and runnable locally,
in Docker, or via GitHub Actions CI.

See [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md) for the full test plan (scope, environment,
test case index, and validation strategy).

## Running the tests locally

Requires Java 17 and Maven.

```bash
mvn test
```

By default, tests run against `https://jsonplaceholder.typicode.com`. To target a
different environment, override without touching any code:

```bash
mvn test -Dbase.uri=https://staging.example.com
# or
BASE_URI=https://staging.example.com mvn test
```

## Viewing the Allure report

Test execution writes results to `target/allure-results`. Generate and open the HTML
report with:

```bash
mvn test
mvn io.qameta.allure:allure-maven:report
open target/site/allure-maven-plugin/index.html   # or: xdg-open on Linux
```

Or generate and serve it in one step:

```bash
mvn io.qameta.allure:allure-maven:serve
```

Tests are annotated with `@Epic`/`@Feature`/`@Story`/`@Severity`/`@Description`, so the
report's **Behaviors** tab groups results as Epic (`JSONPlaceholder API`) > Feature (one
per resource, e.g. `Comments`) > Story (one per HTTP verb + test-plan case, e.g.
"POST - Create with malformed JSON"). `@DisplayName` is kept short (just the id and the
scenario), and each test's page shows a **Description** with the actual expected outcome
- including the full known-fragile rationale for the tests below, so nothing gets
cross-referenced back to this file just to understand a result. Severity marks those
known-fragile tests as `MINOR` so they stand out from core-contract failures.

Every test's page also has a **Request**/**Response** attachment (method, URL, headers,
body, status, and a ready-to-run `curl` repro), via the `AllureRestAssured` filter
registered in `BaseTest` - so a failure can be diagnosed straight from the report
without re-running anything locally.

## Running in Docker

```bash
docker build -t api-testing .
docker run --rm -v "$(pwd)/target/allure-results:/app/target/allure-results" api-testing
```

The container runs `mvn test` against the default base URL; override it with
`-e BASE_URI=...` on `docker run`. The image is a multi-stage build on the Alpine
variant of the Maven/Temurin image: a `builder` stage resolves dependencies and
pre-compiles the test sources into a warm local Maven repo, and the final stage reuses
that repo so starting the container doesn't re-resolve anything from the network.

## CI/CD

Every push and pull request to `main` triggers [`.github/workflows/ci.yml`](.github/workflows/ci.yml):

- **test** - runs the suite natively, generates the Allure report, and uploads both the
  report and raw results as build artifacts.
- **docker** - builds the Docker image and runs the suite inside the container, uploading
  its results as a separate artifact.
- **deploy-report** - on pushes to `main` only, publishes the latest Allure report to
  GitHub Pages: **https://el-gibbor.github.io/api-testing/**

Artifacts from every run are attached under the GitHub Actions summary page; the Pages
site always reflects the most recent `main` build.
