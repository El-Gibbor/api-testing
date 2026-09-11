package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.User;
import com.amalitech.apitesting.utils.TestDataLoader;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * USR-POST-01, USR-POST-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("POST /users")
class CreateUserTest extends BaseTest {

    @Test
    @DisplayName("USR-POST-01: POST /users creates a new user and echoes the submitted fields")
    @Story("Create a new user")
    @Severity(SeverityLevel.CRITICAL)
    void createUser_returnsCreatedUser() {
        User newUser = TestDataLoader.load("new-user.json", User.class);

        User created = given()
            .contentType(ContentType.JSON)
            .body(newUser)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"))
        .extract()
            .as(User.class);

        assertThat(created.id(), notNullValue());
        assertThat(created.name(), equalTo(newUser.name()));
        assertThat(created.username(), equalTo(newUser.username()));
        assertThat(created.email(), equalTo(newUser.email()));
        assertThat(created.address(), equalTo(newUser.address()));
        assertThat(created.company(), equalTo(newUser.company()));
    }

    @Test
    @DisplayName("USR-POST-02: POST /users with an empty body still returns 201 with a generated id "
        + "(documented behavior of the fake API, which does not validate payloads)")
    @Story("Create with an empty body")
    @Severity(SeverityLevel.NORMAL)
    void createUser_emptyBody_stillReturnsCreatedWithGeneratedId() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .header("Location", containsString("/users/"))
            .body("id", notNullValue());
    }
}
