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
 * ALB-DELETE-01, ALB-DELETE-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Albums")
class DeleteAlbumTest extends BaseTest {

    @Test
    @DisplayName("ALB-DELETE-01: DELETE /albums/{id} removes an existing album")
    @Story("DELETE - Delete an existing album")
    @Severity(SeverityLevel.CRITICAL)
    void deleteAlbum_returnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete("/albums/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo("{}"));
    }

    @Test
    @DisplayName("ALB-DELETE-02: DELETE /albums/{id} for a non-existent album is idempotent "
        + "(documented behavior: the fake API returns 200 regardless of whether the id exists)")
    @Story("DELETE - Delete a non-existent album")
    @Severity(SeverityLevel.NORMAL)
    void deleteAlbum_nonExistentId_stillReturnsOk() {
        given()
            .pathParam("id", 999_999)
        .when()
            .delete("/albums/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));
    }
}
