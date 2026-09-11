package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * TOD-DELETE-01, TOD-DELETE-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Todos")
class DeleteTodoTest extends BaseTest {

    @Test
    @DisplayName("TOD-DELETE-01: DELETE /todos/{id} removes an existing todo")
    @Story("DELETE - Delete an existing todo")
    @Severity(SeverityLevel.CRITICAL)
    void deleteTodo_returnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete("/todos/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo("{}"));
    }

    @Test
    @DisplayName("TOD-DELETE-02: DELETE /todos/{id} for a non-existent todo is idempotent "
        + "(documented behavior: the fake API returns 200 regardless of whether the id exists)")
    @Story("DELETE - Delete a non-existent todo")
    @Severity(SeverityLevel.NORMAL)
    void deleteTodo_nonExistentId_stillReturnsOk() {
        given()
            .pathParam("id", 999_999)
        .when()
            .delete("/todos/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));
    }
}
