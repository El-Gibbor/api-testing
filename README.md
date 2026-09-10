# API Testing with REST Assured

Automated test suite for the [JSONPlaceholder](https://jsonplaceholder.typicode.com/) fake
REST API, built with REST Assured, JUnit 5, and Allure reporting, and runnable locally,
in Docker, or via GitHub Actions CI.

See [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md) for the full test plan (scope, environment,
test case index, and validation strategy).

## Tech stack

| Concern | Tool |
|---------|------|
| Language / build | Java 17, Maven |
| HTTP client / assertions | REST Assured 5.x |
| Test framework | JUnit 5 |
| Schema validation | REST Assured `json-schema-validator` |
| Reporting | Allure 2.x |
| Containerization | Docker |
| CI/CD | GitHub Actions |

## Project structure

```
src/test/java/com/amalitech/apitesting/
  base/     BaseTest - shared REST Assured configuration
  models/   Post, Comment - Jackson-backed record models
  utils/    ConfigReader, TestDataLoader
  tests/    one test class per HTTP verb / resource behavior
src/test/resources/
  config.properties  default environment configuration
  schemas/           JSON Schema files used for contract validation
  testdata/          JSON fixtures for POST/PUT request bodies
docs/
  TEST_PLAN.md       test plan document
```

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

## Running in Docker

```bash
docker build -t api-testing .
docker run --rm -v "$(pwd)/target/allure-results:/app/target/allure-results" api-testing
```

The container runs `mvn test` against the default base URL; override it with
`-e BASE_URI=...` on `docker run`.

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
