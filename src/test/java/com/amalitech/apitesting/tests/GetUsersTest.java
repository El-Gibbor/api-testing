package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.User;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * USR-GET-01, USR-GET-02, USR-GET-03, USR-GET-04 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("GET /users")
class GetUsersTest extends BaseTest {

    @Test
    @DisplayName("USR-GET-01: GET /users returns all users with a valid schema")
    @Story("List all users")
    @Severity(SeverityLevel.CRITICAL)
    void getAllUsers_returnsUsersList() {
        User[] users = given()
            .when()
                .get("/users")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/user-array-schema.json"))
            .extract()
                .as(User[].class);

        assertThat(users.length, greaterThan(0));
        assertThat(users[0].id(), notNullValue());
    }

    @Test
    @DisplayName("USR-GET-02: GET /users/{id} returns the requested user")
    @Story("Fetch a single user")
    @Severity(SeverityLevel.CRITICAL)
    void getUserById_returnsMatchingUser() {
        int userId = 1;

        User user = given()
            .pathParam("id", userId)
            .when()
                .get("/users/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"))
            .extract()
                .as(User.class);

        assertThat(user.id(), equalTo(userId));
        assertThat(user.name(), not(emptyOrNullString()));
        assertThat(user.username(), not(emptyOrNullString()));
        assertThat(user.email(), not(emptyOrNullString()));
        assertThat(user.address(), notNullValue());
        assertThat(user.address().geo(), notNullValue());
        assertThat(user.company(), notNullValue());
    }

    @Test
    @DisplayName("USR-GET-03: GET /users/{id} for a non-existent user returns 404")
    @Story("Fetch a non-existent user")
    @Severity(SeverityLevel.NORMAL)
    void getUserById_nonExistentId_returnsNotFound() {
        given()
            .pathParam("id", 999_999)
        .when()
            .get("/users/{id}")
        .then()
            .statusCode(404);
    }

    @ParameterizedTest(name = "GET /users/{0} returns 404")
    @ValueSource(strings = {"abc", "0", "-1"})
    @DisplayName("USR-GET-04: GET /users/{id} returns 404 for invalid ids (non-numeric, zero, negative)")
    @Story("Fetch with an invalid id")
    @Severity(SeverityLevel.NORMAL)
    void getUserById_invalidId_returnsNotFound(String invalidId) {
        given()
            .pathParam("id", invalidId)
        .when()
            .get("/users/{id}")
        .then()
            .statusCode(404);
    }
}
