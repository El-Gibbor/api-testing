package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.User;
import com.amalitech.apitesting.utils.TestDataLoader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * USR-POST-01, USR-POST-02, USR-POST-03, USR-POST-04 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Users")
class CreateUserTest extends BaseTest {

    @Test
    @DisplayName("USR-POST-01: POST /users creates a new user and echoes the submitted fields")
    @Description("Returns 201 with the created user echoing the submitted fields, including the nested address and company objects, and a generated id.")
    @Story("POST - Create a new user")
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
    @DisplayName("USR-POST-02: POST /users with an empty body still succeeds")
    @Description("Documented behavior of the fake API: it does not validate payloads, so an empty body still returns 201 with a generated id.")
    @Story("POST - Create with an empty body")
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

    @Test
    @DisplayName("USR-POST-03: POST /users with a non-JSON Content-Type is not parsed as JSON")
    @Description("Documented quirk: the fake API silently misinterprets the raw body instead of rejecting it, still returning 201.")
    @Story("POST - Create with a non-JSON Content-Type")
    @Severity(SeverityLevel.NORMAL)
    void createUser_nonJsonContentType_bodyIsNotParsedAsJson() {
        // REST Assured appends a default charset (ISO-8859-1) to the Content-Type header unless
        // told not to; that charset alone triggers a different crash (UnsupportedMediaTypeError)
        // than the one this test targets, so it must be disabled here.
        given()
            .config(RestAssuredConfig.config().encoderConfig(
                EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
            .contentType("application/x-www-form-urlencoded")
            .body("{\"name\":\"x\"}")
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("USR-POST-04: POST /users with malformed JSON returns an unhandled server error")
    @Description("KNOWN FRAGILE: pins to the same body-parser/json-server crash as POST-04 in CreatePostTest, not a documented 400 contract. If this starts failing, it likely means upstream added input validation - relax this assertion rather than assuming a regression.")
    @Story("POST - Create with malformed JSON")
    @Severity(SeverityLevel.MINOR)
    void createUser_malformedJson_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json")
        .when()
            .post("/users")
        .then()
            .statusCode(500);
    }
}
