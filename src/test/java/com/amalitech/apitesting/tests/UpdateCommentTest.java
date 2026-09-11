package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Comment;
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
 * COM-PUT-01, COM-PUT-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Comments")
class UpdateCommentTest extends BaseTest {

    @Test
    @DisplayName("COM-PUT-01: PUT /comments/{id} fully updates an existing comment")
    @Description("Returns 200 with the response reflecting the updated fields.")
    @Story("PUT - Fully update an existing comment")
    @Severity(SeverityLevel.CRITICAL)
    void updateComment_returnsUpdatedComment() {
        Comment updatedComment = TestDataLoader.load("updated-comment.json", Comment.class);

        Comment updated = given()
            .contentType(ContentType.JSON)
            .pathParam("id", updatedComment.id())
            .body(updatedComment)
        .when()
            .put("/comments/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/comment-schema.json"))
        .extract()
            .as(Comment.class);

        assertThat(updated.id(), equalTo(updatedComment.id()));
        assertThat(updated.postId(), equalTo(updatedComment.postId()));
        assertThat(updated.name(), equalTo(updatedComment.name()));
        assertThat(updated.email(), equalTo(updatedComment.email()));
        assertThat(updated.body(), equalTo(updatedComment.body()));
    }

    @Test
    @DisplayName("COM-PUT-02: PUT /comments/{id} for a non-existent comment returns a server error")
    @Description("KNOWN FRAGILE: same underlying backend crash as PUT-02/PUT-03 in UpdatePostTest - not a documented contract. If this starts failing, it likely means upstream fixed the bug - relax this assertion rather than assuming a regression.")
    @Story("PUT - Update a non-existent comment")
    @Severity(SeverityLevel.MINOR)
    void updateComment_nonExistentId_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 999_999)
            .body("{\"id\":999999,\"postId\":1,\"name\":\"updated\",\"email\":\"updated@example.com\","
                + "\"body\":\"updated body\"}")
        .when()
            .put("/comments/{id}")
        .then()
            .statusCode(500);
    }
}
