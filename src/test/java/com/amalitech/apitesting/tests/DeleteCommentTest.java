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
 * COM-DELETE-01, COM-DELETE-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Comments")
class DeleteCommentTest extends BaseTest {

    @Test
    @DisplayName("COM-DELETE-01: DELETE /comments/{id} removes an existing comment")
    @Story("DELETE - Delete an existing comment")
    @Severity(SeverityLevel.CRITICAL)
    void deleteComment_returnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete("/comments/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo("{}"));
    }

    @Test
    @DisplayName("COM-DELETE-02: DELETE /comments/{id} for a non-existent comment is idempotent "
        + "(documented behavior: the fake API returns 200 regardless of whether the id exists)")
    @Story("DELETE - Delete a non-existent comment")
    @Severity(SeverityLevel.NORMAL)
    void deleteComment_nonExistentId_stillReturnsOk() {
        given()
            .pathParam("id", 999_999)
        .when()
            .delete("/comments/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));
    }
}
