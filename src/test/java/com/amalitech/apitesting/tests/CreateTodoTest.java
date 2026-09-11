package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Todo;
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
 * TOD-POST-01, TOD-POST-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("POST /todos")
class CreateTodoTest extends BaseTest {

    @Test
    @DisplayName("TOD-POST-01: POST /todos creates a new todo and echoes the submitted fields")
    @Story("Create a new todo")
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
    @Story("Create with an empty body")
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
}
