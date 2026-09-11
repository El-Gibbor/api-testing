package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Todo;
import io.qameta.allure.Description;
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

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TOD-GET-01, TOD-GET-02, TOD-GET-03, TOD-GET-04, TOD-GET-05 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Todos")
class GetTodosTest extends BaseTest {

    @Test
    @DisplayName("TOD-GET-01: GET /todos returns all todos with a valid schema")
    @Description("Returns 200 with a non-empty array of todos, the Content-Type header set to application/json, and a body that matches the todos array JSON schema.")
    @Story("GET - List all todos")
    @Severity(SeverityLevel.CRITICAL)
    void getAllTodos_returnsTodosList() {
        Todo[] todos = given()
            .when()
                .get("/todos")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/todo-array-schema.json"))
            .extract()
                .as(Todo[].class);

        assertThat(todos.length, greaterThan(0));
        assertThat(todos[0].id(), notNullValue());
    }

    @Test
    @DisplayName("TOD-GET-02: GET /todos/{id} returns the requested todo")
    @Description("Returns 200 with the requested todo's fields, validated against the todo JSON schema.")
    @Story("GET - Fetch a single todo")
    @Severity(SeverityLevel.CRITICAL)
    void getTodoById_returnsMatchingTodo() {
        int todoId = 1;

        Todo todo = given()
            .pathParam("id", todoId)
            .when()
                .get("/todos/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/todo-schema.json"))
            .extract()
                .as(Todo.class);

        assertThat(todo.id(), equalTo(todoId));
        assertThat(todo.userId(), notNullValue());
        assertThat(todo.title(), not(emptyOrNullString()));
        assertThat(todo.completed(), notNullValue());
    }

    @Test
    @DisplayName("TOD-GET-03: GET /todos/{id} for a non-existent todo returns 404")
    @Description("Returns 404 when the requested todo id does not exist.")
    @Story("GET - Fetch a non-existent todo")
    @Severity(SeverityLevel.NORMAL)
    void getTodoById_nonExistentId_returnsNotFound() {
        given()
            .pathParam("id", 999_999)
        .when()
            .get("/todos/{id}")
        .then()
            .statusCode(404);
    }

    @ParameterizedTest(name = "GET /todos/{0} returns 404")
    @ValueSource(strings = {"abc", "0", "-1"})
    @DisplayName("TOD-GET-04: GET /todos/{id} returns 404 for invalid ids (non-numeric, zero, negative)")
    @Description("Returns 404 for a non-numeric, zero, and negative id alike.")
    @Story("GET - Fetch with an invalid id")
    @Severity(SeverityLevel.NORMAL)
    void getTodoById_invalidId_returnsNotFound(String invalidId) {
        given()
            .pathParam("id", invalidId)
        .when()
            .get("/todos/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("TOD-GET-05: GET /todos?userId={id}&completed={bool} returns only matching todos")
    @Description("Returns 200 with only the todos matching both the given userId and completed filters, validated against the todos array schema.")
    @Story("GET - Filter todos by multiple query parameters")
    @Severity(SeverityLevel.NORMAL)
    void getTodosByUserIdAndCompleted_returnsOnlyMatchingTodos() {
        int userId = 1;
        boolean completed = false;

        Todo[] todos = given()
            .queryParam("userId", userId)
            .queryParam("completed", completed)
            .when()
                .get("/todos")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/todo-array-schema.json"))
            .extract()
                .as(Todo[].class);

        assertThat(todos.length, greaterThan(0));
        assertThat(Arrays.stream(todos)
            .allMatch(todo -> todo.userId().equals(userId) && todo.completed().equals(completed)), is(true));
    }
}
