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
 * PHO-DELETE-01, PHO-DELETE-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Photos")
class DeletePhotoTest extends BaseTest {

    @Test
    @DisplayName("PHO-DELETE-01: DELETE /photos/{id} removes an existing photo")
    @Story("DELETE - Delete an existing photo")
    @Severity(SeverityLevel.CRITICAL)
    void deletePhoto_returnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete("/photos/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo("{}"));
    }

    @Test
    @DisplayName("PHO-DELETE-02: DELETE /photos/{id} for a non-existent photo is idempotent "
        + "(documented behavior: the fake API returns 200 regardless of whether the id exists)")
    @Story("DELETE - Delete a non-existent photo")
    @Severity(SeverityLevel.NORMAL)
    void deletePhoto_nonExistentId_stillReturnsOk() {
        given()
            .pathParam("id", 999_999)
        .when()
            .delete("/photos/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));
    }
}
