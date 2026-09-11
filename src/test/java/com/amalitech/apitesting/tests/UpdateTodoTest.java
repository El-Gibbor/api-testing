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
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * TOD-PUT-01, TOD-PUT-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Todos")
class UpdateTodoTest extends BaseTest {

    @Test
    @DisplayName("TOD-PUT-01: PUT /todos/{id} fully updates an existing todo")
    @Description("200, response reflects updated fields.")
    @Story("PUT - Fully update an existing todo")
    @Severity(SeverityLevel.CRITICAL)
    void updateTodo_returnsUpdatedTodo() {
        Todo updatedTodo = TestDataLoader.load("updated-todo.json", Todo.class);

        Todo updated = given()
            .contentType(ContentType.JSON)
            .pathParam("id", updatedTodo.id())
            .body(updatedTodo)
        .when()
            .put("/todos/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/todo-schema.json"))
        .extract()
            .as(Todo.class);

        assertThat(updated.id(), equalTo(updatedTodo.id()));
        assertThat(updated.userId(), equalTo(updatedTodo.userId()));
        assertThat(updated.title(), equalTo(updatedTodo.title()));
        assertThat(updated.completed(), equalTo(updatedTodo.completed()));
    }

    @Test
    @DisplayName("TOD-PUT-02: PUT /todos/{id} for a non-existent todo returns a server error "
        + "(KNOWN FRAGILE: same underlying backend crash as PUT-02/PUT-03 in UpdatePostTest - not a "
        + "documented contract. If this starts failing, it likely means upstream fixed the bug - relax "
        + "this assertion rather than assuming a regression.)")
    @Description("500 (known fragile - pins to an upstream json-server bug, not a documented contract).")
    @Story("PUT - Update a non-existent todo")
    @Severity(SeverityLevel.MINOR)
    void updateTodo_nonExistentId_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 999_999)
            .body("{\"id\":999999,\"userId\":1,\"title\":\"updated\",\"completed\":true}")
        .when()
            .put("/todos/{id}")
        .then()
            .statusCode(500);
    }
}
