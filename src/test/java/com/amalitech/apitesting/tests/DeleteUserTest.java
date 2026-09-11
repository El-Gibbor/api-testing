package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
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
import static org.hamcrest.Matchers.equalTo;

/**
 * USR-DELETE-01, USR-DELETE-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Users")
class DeleteUserTest extends BaseTest {

    @Test
    @DisplayName("USR-DELETE-01: DELETE /users/{id} removes an existing user")
    @Description("Returns 200 with an empty response body.")
    @Story("DELETE - Delete an existing user")
    @Severity(SeverityLevel.CRITICAL)
    void deleteUser_returnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete("/users/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo("{}"));
    }

    @Test
    @DisplayName("USR-DELETE-02: DELETE /users/{id} for a non-existent user is idempotent")
    @Description("Documented behavior of the fake API: returns 200 regardless of whether the id exists.")
    @Story("DELETE - Delete a non-existent user")
    @Severity(SeverityLevel.NORMAL)
    void deleteUser_nonExistentId_stillReturnsOk() {
        given()
            .pathParam("id", 999_999)
        .when()
            .delete("/users/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));
    }
}
