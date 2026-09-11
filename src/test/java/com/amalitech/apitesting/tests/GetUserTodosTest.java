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

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * NEST-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Users")
class GetUserTodosTest extends BaseTest {

    @Test
    @DisplayName("NEST-02: GET /users/{id}/todos returns todos belonging to that user")
    @Description("200, all items reference the parent userId, schema.")
    @Story("GET - Fetch nested todos for a user")
    @Severity(SeverityLevel.NORMAL)
    void getTodosForUser_returnsMatchingTodos() {
        int userId = 1;

        Todo[] todos = given()
            .pathParam("id", userId)
            .when()
                .get("/users/{id}/todos")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/todo-array-schema.json"))
            .extract()
                .as(Todo[].class);

        assertThat(todos.length, greaterThan(0));
        assertThat(Arrays.stream(todos).allMatch(todo -> todo.userId().equals(userId)), is(true));
    }
}
