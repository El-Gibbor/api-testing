package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Todo;
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
 * TOD-POST-01, TOD-POST-02, TOD-POST-03, TOD-POST-04 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Todos")
class CreateTodoTest extends BaseTest {

    @Test
    @DisplayName("TOD-POST-01: POST /todos creates a new todo and echoes the submitted fields")
    @Description("201, response echoes submitted fields, generated id present.")
    @Story("POST - Create a new todo")
    @Severity(SeverityLevel.CRITICAL)
    void createTodo_returnsCreatedTodo() {
        Todo newTodo = TestDataLoader.load("new-todo.json", Todo.class);

        Todo created = given()
            .contentType(ContentType.JSON)
            .body(newTodo)
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/todo-schema.json"))
        .extract()
            .as(Todo.class);

        assertThat(created.id(), notNullValue());
        assertThat(created.userId(), equalTo(newTodo.userId()));
        assertThat(created.title(), equalTo(newTodo.title()));
        assertThat(created.completed(), equalTo(newTodo.completed()));
    }

    @Test
    @DisplayName("TOD-POST-02: POST /todos with an empty body still returns 201 with a generated id "
        + "(documented behavior of the fake API, which does not validate payloads)")
    @Description("Documented actual behavior of the fake API.")
    @Story("POST - Create with an empty body")
    @Severity(SeverityLevel.NORMAL)
    void createTodo_emptyBody_stillReturnsCreatedWithGeneratedId() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .header("Location", containsString("/todos/"))
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("TOD-POST-03: POST /todos with a non-JSON Content-Type is not parsed as JSON "
        + "(documented behavior: the fake API silently misinterprets the raw body instead of rejecting it)")
    @Description("201, but the raw body is not parsed as JSON (documented quirk).")
    @Story("POST - Create with a non-JSON Content-Type")
    @Severity(SeverityLevel.NORMAL)
    void createTodo_nonJsonContentType_bodyIsNotParsedAsJson() {
        // REST Assured appends a default charset (ISO-8859-1) to the Content-Type header unless
        // told not to; that charset alone triggers a different crash (UnsupportedMediaTypeError)
        // than the one this test targets, so it must be disabled here.
        given()
            .config(RestAssuredConfig.config().encoderConfig(
                EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
            .contentType("application/x-www-form-urlencoded")
            .body("{\"title\":\"x\"}")
        .when()
            .post("/todos")
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("TOD-POST-04: POST /todos with malformed JSON returns an unhandled server error "
        + "(KNOWN FRAGILE: pins to the same body-parser/json-server crash as POST-04 in CreatePostTest, "
        + "not a documented 400 contract. If this test starts failing, it likely means upstream added "
        + "input validation - relax this assertion rather than assuming a regression.)")
    @Description("500 (known fragile - pins to an upstream json-server bug, not a documented contract).")
    @Story("POST - Create with malformed JSON")
    @Severity(SeverityLevel.MINOR)
    void createTodo_malformedJson_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json")
        .when()
            .post("/todos")
        .then()
            .statusCode(500);
    }
}
