package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Post;
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
import static org.hamcrest.Matchers.equalTo;

/**
 * PUT-01, PUT-02, PUT-03 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("PUT /posts/{id}")
class UpdatePostTest extends BaseTest {

    @Test
    @DisplayName("PUT-01: PUT /posts/{id} fully updates an existing post")
    @Story("Fully update an existing post")
    @Severity(SeverityLevel.CRITICAL)
    void updatePost_returnsUpdatedPost() {
        Post updatedPost = TestDataLoader.load("updated-post.json", Post.class);

        Post updated = given()
            .contentType(ContentType.JSON)
            .pathParam("id", updatedPost.id())
            .body(updatedPost)
        .when()
            .put("/posts/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"))
        .extract()
            .as(Post.class);

        assertThat(updated.id(), equalTo(updatedPost.id()));
        assertThat(updated.userId(), equalTo(updatedPost.userId()));
        assertThat(updated.title(), equalTo(updatedPost.title()));
        assertThat(updated.body(), equalTo(updatedPost.body()));
    }

    @Test
    @DisplayName("PUT-02: PUT /posts/{id} for a non-existent post returns a server error "
        + "(KNOWN FRAGILE: the fake API's backend throws on missing records instead of returning "
        + "404 - not a documented contract. If this starts failing, it likely means upstream fixed "
        + "the bug - relax this assertion rather than assuming a regression.)")
    @Story("Update a non-existent post")
    @Severity(SeverityLevel.MINOR)
    void updatePost_nonExistentId_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 999_999)
            .body("{\"id\":999999,\"userId\":1,\"title\":\"updated\",\"body\":\"updated body\"}")
        .when()
            .put("/posts/{id}")
        .then()
            .statusCode(500);
    }

    @Test
    @DisplayName("PUT-03: PUT /posts/{id} with a non-numeric id returns a server error "
        + "(KNOWN FRAGILE: same underlying backend crash as PUT-02, not a documented contract)")
    @Story("Update with a non-numeric id")
    @Severity(SeverityLevel.MINOR)
    void updatePost_nonNumericId_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", "abc")
            .body("{\"title\":\"updated\"}")
        .when()
            .put("/posts/{id}")
        .then()
            .statusCode(500);
    }
}
