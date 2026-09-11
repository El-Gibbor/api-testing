# API Test Plan: JSONPlaceholder

## 1. Objective

Validate the functional correctness, contract stability, and reliability of the
[JSONPlaceholder](https://jsonplaceholder.typicode.com/) fake REST API using automated
tests built with REST Assured, executed via Maven, reported with Allure, and run both
locally and in a containerized CI/CD pipeline.

## 2. Scope

### In scope

- HTTP methods: `GET`, `POST`, `PUT`, `DELETE`
- Resources: all six JSONPlaceholder resources - `/posts`, `/comments`, `/albums`,
  `/photos`, `/todos`, `/users`
- Resource relationships: `/posts/{id}/comments`, `/users/{id}/albums`,
  `/users/{id}/todos`, `/users/{id}/posts`, `/albums/{id}/photos`
- Query-parameter filtering (e.g. `GET /posts?userId=1`) on every top-level resource
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

Every resource below follows the same shape: list / fetch-by-id / not-found / invalid-id
/ query-filter under `GET`, create / empty-body / non-JSON `Content-Type` / malformed
JSON under `POST`, full update / update-a-missing-record under `PUT`, and delete /
delete-a-missing-record under `DELETE`. Each table is the concrete, enumerated index for
its resource; test IDs map 1:1 to `@DisplayName` values on the corresponding test method.

### Posts (`/posts`)

| ID | Method | Endpoint | Description | Key Assertions |
|----|--------|----------|--------------|-----------------|
| GET-01 | GET | `/posts` | List all posts | 200, non-empty array, `Content-Type: application/json`, schema |
| GET-02 | GET | `/posts/{id}` | Fetch a single post | 200, body fields match, schema |
| GET-03 | GET | `/posts/{id}` | Fetch a non-existent post | 404 |
| GET-04 | GET | `/posts/{id}/comments` | Fetch nested comments for a post | 200, all items reference the parent `postId`, schema |
| GET-05 | GET | `/posts/{id}` | Fetch with an invalid id (non-numeric, zero, negative) | 404 for each case |
| GET-06 | GET | `/posts?userId={id}` | Filter posts by query parameter | 200, all items reference the given `userId`, schema |
| POST-01 | POST | `/posts` | Create a new post | 201, response echoes submitted fields, generated `id` present |
| POST-02 | POST | `/posts` | Create with an empty body | Documented actual behavior of the fake API |
| POST-03 | POST | `/posts` | Create with a non-JSON `Content-Type` | 201, but the raw body is not parsed as JSON (documented quirk) |
| POST-04 | POST | `/posts` | Create with malformed JSON syntax | 500 (**known fragile**, see Section 6) |
| PUT-01 | PUT | `/posts/{id}` | Fully update an existing post | 200, response reflects updated fields |
| PUT-02 | PUT | `/posts/{id}` | Update a non-existent post | 500 (**known fragile**, see Section 6) |
| PUT-03 | PUT | `/posts/{id}` | Update with a non-numeric id | 500 (**known fragile**, see Section 6) |
| DELETE-01 | DELETE | `/posts/{id}` | Delete an existing post | 200, empty response body |
| DELETE-02 | DELETE | `/posts/{id}` | Delete a non-existent post | Documented actual behavior of the fake API |

### Comments (`/comments`)

| ID | Method | Endpoint | Description | Key Assertions |
|----|--------|----------|--------------|-----------------|
| COM-GET-01 | GET | `/comments` | List all comments | 200, non-empty array, `Content-Type: application/json`, schema |
| COM-GET-02 | GET | `/comments/{id}` | Fetch a single comment | 200, body fields match, schema |
| COM-GET-03 | GET | `/comments/{id}` | Fetch a non-existent comment | 404 |
| COM-GET-04 | GET | `/comments/{id}` | Fetch with an invalid id (non-numeric, zero, negative) | 404 for each case |
| COM-GET-05 | GET | `/comments?postId={id}` | Filter comments by query parameter | 200, all items reference the given `postId`, schema |
| COM-POST-01 | POST | `/comments` | Create a new comment | 201, response echoes submitted fields, generated `id` present |
| COM-POST-02 | POST | `/comments` | Create with an empty body | Documented actual behavior of the fake API |
| COM-POST-03 | POST | `/comments` | Create with a non-JSON `Content-Type` | 201, but the raw body is not parsed as JSON (documented quirk) |
| COM-POST-04 | POST | `/comments` | Create with malformed JSON syntax | 500 (**known fragile**, see Section 6) |
| COM-PUT-01 | PUT | `/comments/{id}` | Fully update an existing comment | 200, response reflects updated fields |
| COM-PUT-02 | PUT | `/comments/{id}` | Update a non-existent comment | 500 (**known fragile**, see Section 6) |
| COM-DELETE-01 | DELETE | `/comments/{id}` | Delete an existing comment | 200, empty response body |
| COM-DELETE-02 | DELETE | `/comments/{id}` | Delete a non-existent comment | Documented actual behavior of the fake API |

### Albums (`/albums`)

| ID | Method | Endpoint | Description | Key Assertions |
|----|--------|----------|--------------|-----------------|
| ALB-GET-01 | GET | `/albums` | List all albums | 200, non-empty array, `Content-Type: application/json`, schema |
| ALB-GET-02 | GET | `/albums/{id}` | Fetch a single album | 200, body fields match, schema |
| ALB-GET-03 | GET | `/albums/{id}` | Fetch a non-existent album | 404 |
| ALB-GET-04 | GET | `/albums/{id}` | Fetch with an invalid id (non-numeric, zero, negative) | 404 for each case |
| ALB-GET-05 | GET | `/albums?userId={id}` | Filter albums by query parameter | 200, all items reference the given `userId`, schema |
| ALB-POST-01 | POST | `/albums` | Create a new album | 201, response echoes submitted fields, generated `id` present |
| ALB-POST-02 | POST | `/albums` | Create with an empty body | Documented actual behavior of the fake API |
| ALB-POST-03 | POST | `/albums` | Create with a non-JSON `Content-Type` | 201, but the raw body is not parsed as JSON (documented quirk) |
| ALB-POST-04 | POST | `/albums` | Create with malformed JSON syntax | 500 (**known fragile**, see Section 6) |
| ALB-PUT-01 | PUT | `/albums/{id}` | Fully update an existing album | 200, response reflects updated fields |
| ALB-PUT-02 | PUT | `/albums/{id}` | Update a non-existent album | 500 (**known fragile**, see Section 6) |
| ALB-DELETE-01 | DELETE | `/albums/{id}` | Delete an existing album | 200, empty response body |
| ALB-DELETE-02 | DELETE | `/albums/{id}` | Delete a non-existent album | Documented actual behavior of the fake API |

### Photos (`/photos`)

| ID | Method | Endpoint | Description | Key Assertions |
|----|--------|----------|--------------|-----------------|
| PHO-GET-01 | GET | `/photos` | List all photos | 200, non-empty array, `Content-Type: application/json`, schema |
| PHO-GET-02 | GET | `/photos/{id}` | Fetch a single photo | 200, body fields match, schema |
| PHO-GET-03 | GET | `/photos/{id}` | Fetch a non-existent photo | 404 |
| PHO-GET-04 | GET | `/photos/{id}` | Fetch with an invalid id (non-numeric, zero, negative) | 404 for each case |
| PHO-GET-05 | GET | `/photos?albumId={id}` | Filter photos by query parameter | 200, all items reference the given `albumId`, schema |
| PHO-POST-01 | POST | `/photos` | Create a new photo | 201, response echoes submitted fields, generated `id` present |
| PHO-POST-02 | POST | `/photos` | Create with an empty body | Documented actual behavior of the fake API |
| PHO-POST-03 | POST | `/photos` | Create with a non-JSON `Content-Type` | 201, but the raw body is not parsed as JSON (documented quirk) |
| PHO-POST-04 | POST | `/photos` | Create with malformed JSON syntax | 500 (**known fragile**, see Section 6) |
| PHO-PUT-01 | PUT | `/photos/{id}` | Fully update an existing photo | 200, response reflects updated fields |
| PHO-PUT-02 | PUT | `/photos/{id}` | Update a non-existent photo | 500 (**known fragile**, see Section 6) |
| PHO-DELETE-01 | DELETE | `/photos/{id}` | Delete an existing photo | 200, empty response body |
| PHO-DELETE-02 | DELETE | `/photos/{id}` | Delete a non-existent photo | Documented actual behavior of the fake API |

### Todos (`/todos`)

| ID | Method | Endpoint | Description | Key Assertions |
|----|--------|----------|--------------|-----------------|
| TOD-GET-01 | GET | `/todos` | List all todos | 200, non-empty array, `Content-Type: application/json`, schema |
| TOD-GET-02 | GET | `/todos/{id}` | Fetch a single todo | 200, body fields match, schema |
| TOD-GET-03 | GET | `/todos/{id}` | Fetch a non-existent todo | 404 |
| TOD-GET-04 | GET | `/todos/{id}` | Fetch with an invalid id (non-numeric, zero, negative) | 404 for each case |
| TOD-GET-05 | GET | `/todos?userId={id}&completed={bool}` | Filter todos by multiple query parameters | 200, all items match both filters, schema |
| TOD-POST-01 | POST | `/todos` | Create a new todo | 201, response echoes submitted fields, generated `id` present |
| TOD-POST-02 | POST | `/todos` | Create with an empty body | Documented actual behavior of the fake API |
| TOD-POST-03 | POST | `/todos` | Create with a non-JSON `Content-Type` | 201, but the raw body is not parsed as JSON (documented quirk) |
| TOD-POST-04 | POST | `/todos` | Create with malformed JSON syntax | 500 (**known fragile**, see Section 6) |
| TOD-PUT-01 | PUT | `/todos/{id}` | Fully update an existing todo | 200, response reflects updated fields |
| TOD-PUT-02 | PUT | `/todos/{id}` | Update a non-existent todo | 500 (**known fragile**, see Section 6) |
| TOD-DELETE-01 | DELETE | `/todos/{id}` | Delete an existing todo | 200, empty response body |
| TOD-DELETE-02 | DELETE | `/todos/{id}` | Delete a non-existent todo | Documented actual behavior of the fake API |

### Users (`/users`)

| ID | Method | Endpoint | Description | Key Assertions |
|----|--------|----------|--------------|-----------------|
| USR-GET-01 | GET | `/users` | List all users | 200, non-empty array, `Content-Type: application/json`, schema |
| USR-GET-02 | GET | `/users/{id}` | Fetch a single user | 200, body fields (incl. nested `address`/`company`) match, schema |
| USR-GET-03 | GET | `/users/{id}` | Fetch a non-existent user | 404 |
| USR-GET-04 | GET | `/users/{id}` | Fetch with an invalid id (non-numeric, zero, negative) | 404 for each case |
| USR-GET-05 | GET | `/users?username={username}` | Filter users by query parameter | 200, the returned user matches the given `username`, schema |
| USR-POST-01 | POST | `/users` | Create a new user | 201, response echoes submitted fields (incl. nested objects), generated `id` present |
| USR-POST-02 | POST | `/users` | Create with an empty body | Documented actual behavior of the fake API |
| USR-POST-03 | POST | `/users` | Create with a non-JSON `Content-Type` | 201, but the raw body is not parsed as JSON (documented quirk) |
| USR-POST-04 | POST | `/users` | Create with malformed JSON syntax | 500 (**known fragile**, see Section 6) |
| USR-PUT-01 | PUT | `/users/{id}` | Fully update an existing user | 200, response reflects updated fields |
| USR-PUT-02 | PUT | `/users/{id}` | Update a non-existent user | 500 (**known fragile**, see Section 6) |
| USR-DELETE-01 | DELETE | `/users/{id}` | Delete an existing user | 200, empty response body |
| USR-DELETE-02 | DELETE | `/users/{id}` | Delete a non-existent user | Documented actual behavior of the fake API |

### Cross-resource relationships

Beyond `GET-04` (`/posts/{id}/comments`, above), the following parent/child
relationships JSONPlaceholder exposes are also verified directly:

| ID | Method | Endpoint | Description | Key Assertions |
|----|--------|----------|--------------|-----------------|
| NEST-01 | GET | `/users/{id}/albums` | Fetch nested albums for a user | 200, all items reference the parent `userId`, schema |
| NEST-02 | GET | `/users/{id}/todos` | Fetch nested todos for a user | 200, all items reference the parent `userId`, schema |
| NEST-03 | GET | `/users/{id}/posts` | Fetch nested posts for a user | 200, all items reference the parent `userId`, schema |
| NEST-04 | GET | `/albums/{id}/photos` | Fetch nested photos for an album | 200, all items reference the parent `albumId`, schema |

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

### Known-fragile tests

`POST-04`, `PUT-02`, and `PUT-03` on `/posts`, and the equivalent `{RES}-POST-04` (in
every `Create*Test`) and `{RES}-PUT-02` (in every `Update*Test`) tests for `/comments`,
`/albums`, `/photos`, `/todos`, and `/users`, all assert on a `500` response caused by
an unhandled crash in JSONPlaceholder's backend (`json-server`/`body-parser` throwing on
malformed input or a missing record) rather than a documented error contract. They are
deterministic today, but they pin to an upstream *bug*, not a *guarantee*: if
JSONPlaceholder ever patches this, these tests will start failing with no code change on
our side. If that happens, relax the assertion (e.g. to "not 2xx") rather than treating
it as a regression. This is a known, accepted trade-off of testing against a public
third-party fake API rather than a service we control. The crash (and the `POST-03`
non-JSON-`Content-Type` quirk beside it) was confirmed via manual `curl` requests to
reproduce identically across all six resources before any of these tests were written,
so it is treated as one backend-wide bug/quirk pair rather than six independent ones.

## 7. Reporting

Test results are collected as Allure results (`allure-results/`) during `mvn test` and
rendered into a browsable HTML report via the Allure Maven plugin. Tests are annotated
with `@Epic`/`@Feature`/`@Story`/`@Severity`/`@Description` so the report's
**Behaviors** tab groups results as Epic (`JSONPlaceholder API`) > Feature (one per
resource, e.g. `Comments`, `Users`) > Story (one per HTTP verb + test-plan case, e.g.
"POST - Create a new comment", "GET - Fetch nested todos for a user"). `@DisplayName`
stays short (id + scenario only); each test's page carries the full expected-outcome
Description instead - including, for the known-fragile tests below, the complete
rationale for why the assertion is pinned to a bug rather than a contract. Those tests
are marked `MINOR` severity to separate them from core-contract failures. The
`AllureRestAssured`
filter (registered in `BaseTest`) additionally attaches each call's full request and
response - method, URL, headers, body, status, and a `curl` repro - to its test in the
report, so execution detail is available without re-running anything locally. See the
root `README.md` for the exact commands, and `https://el-gibbor.github.io/api-testing/`
for the latest published report.

## 8. CI/CD

Every push and pull request to `main` triggers the GitHub Actions workflow defined in
[`.github/workflows/ci.yml`](../.github/workflows/ci.yml). It runs the suite natively,
runs it again inside the Docker image, generates the Allure report, uploads both the
report and raw results as build artifacts, and - on pushes to `main` - publishes the
report to GitHub Pages. See the root `README.md`'s CI/CD section for the full per-job
breakdown.
