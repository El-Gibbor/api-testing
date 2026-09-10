# API Test Plan: JSONPlaceholder

## 1. Objective

Validate the functional correctness, contract stability, and reliability of the
[JSONPlaceholder](https://jsonplaceholder.typicode.com/) fake REST API using automated
tests built with REST Assured, executed via Maven, reported with Allure, and run both
locally and in a containerized CI/CD pipeline.

## 2. Scope

### In scope

- HTTP methods: `GET`, `POST`, `PUT`, `DELETE`
- Resources: `/posts`, `/comments`, `/users` (primary), with `/posts/{id}/comments` as a
  nested-resource example
- Validations:
  - HTTP status codes (success and not-found paths)
  - Response body structure and field values
  - Response headers (e.g. `Content-Type`)
  - Response payloads against JSON Schema
- Environments: local developer machine, Docker container, GitHub Actions CI

### Out of scope

- Load/performance testing
- Security/penetration testing
- UI testing
- Authentication flows (JSONPlaceholder is unauthenticated)

## 3. Test Environment

| Item | Detail |
|------|--------|
| Base URL | `https://jsonplaceholder.typicode.com` (overridable via `base.uri` system property or `BASE_URI` env var, see `src/test/resources/config.properties`) |
| Language / Build | Java 17, Maven |
| Test framework | JUnit 5 |
| HTTP/assertion library | REST Assured 5.x |
| Schema validation | `json-schema-validator` (REST Assured module) |
| Reporting | Allure 2.x |
| Containerization | Docker |
| CI/CD | GitHub Actions |

Since JSONPlaceholder is a public fake API, all "writes" (`POST`/`PUT`/`DELETE`) are
simulated by the service: it returns realistic responses (e.g. a fabricated new `id`)
but does not persist changes. Tests assert on the *contract* of these responses rather
than on server-side persistence.

## 4. Test Approach

Each resource gets one test class per concern, sharing a common `BaseTest` for REST
Assured configuration:

| Layer | Purpose |
|-------|---------|
| `base` | Shared REST Assured setup (base URI, logging filters) |
| `models` | POJOs for request/response (de)serialization |
| `utils` | `ConfigReader` (environment config), `TestDataLoader` (JSON fixtures) |
| `tests` | One class per HTTP verb / resource behavior |

Test data for `POST`/`PUT` request bodies is stored as JSON fixtures under
`src/test/resources/testdata/` and loaded into POJOs via `TestDataLoader`, rather than
hardcoded inline in test methods, keeping payloads reusable and easy to update.

## 5. Test Cases Summary

| ID | Method | Endpoint | Description | Key Assertions |
|----|--------|----------|--------------|-----------------|
| GET-01 | GET | `/posts` | List all posts | 200, non-empty array, `Content-Type: application/json`, schema |
| GET-02 | GET | `/posts/{id}` | Fetch a single post | 200, body fields match, schema |
| GET-03 | GET | `/posts/{id}` | Fetch a non-existent post | 404 |
| GET-04 | GET | `/posts/{id}/comments` | Fetch nested comments for a post | 200, all items reference the parent `postId`, schema |
| POST-01 | POST | `/posts` | Create a new post | 201, response echoes submitted fields, generated `id` present |
| POST-02 | POST | `/posts` | Create with malformed/empty body | Documented actual behavior of the fake API |
| PUT-01 | PUT | `/posts/{id}` | Fully update an existing post | 200, response reflects updated fields |
| PUT-02 | PUT | `/posts/{id}` | Update a non-existent post | Documented actual behavior of the fake API |
| DELETE-01 | DELETE | `/posts/{id}` | Delete an existing post | 200, empty response body |

This table is the living index of automated coverage; individual test methods in
`src/test/java/com/amalitech/apitesting/tests` map 1:1 to these IDs via their
`@DisplayName`.

## 6. Validation Strategy

- **Status codes**: asserted on every request (success and negative paths).
- **Response body**: deserialized into POJOs and asserted field-by-field, or asserted
  via REST Assured's body/path matchers for collection-level checks.
- **Headers**: `Content-Type` (and others where relevant) asserted per response.
- **JSON Schema**: representative endpoints validated against schema files in
  `src/test/resources/schemas/`, catching unexpected structural/contract drift.

## 7. Reporting

Test results are collected as Allure results (`allure-results/`) during `mvn test` and
rendered into a browsable HTML report via the Allure Maven plugin. See the root
`README.md` for the exact commands.

## 8. CI/CD

Every push and pull request triggers a GitHub Actions workflow that builds the project,
runs the full test suite, and publishes the Allure report as a build artifact. See
`.github/workflows/` once added.
