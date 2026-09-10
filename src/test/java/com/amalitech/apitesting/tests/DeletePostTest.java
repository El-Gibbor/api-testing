package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * DELETE-01, DELETE-02 from docs/TEST_PLAN.md.
 */
class DeletePostTest extends BaseTest {

    @Test
    @DisplayName("DELETE-01: DELETE /posts/{id} removes an existing post")
    void deletePost_returnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete("/posts/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo("{}"));
    }

    @Test
    @DisplayName("DELETE-02: DELETE /posts/{id} for a non-existent post is idempotent "
        + "(documented behavior: the fake API returns 200 regardless of whether the id exists)")
    void deletePost_nonExistentId_stillReturnsOk() {
        given()
            .pathParam("id", 999_999)
        .when()
            .delete("/posts/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));
    }
}
