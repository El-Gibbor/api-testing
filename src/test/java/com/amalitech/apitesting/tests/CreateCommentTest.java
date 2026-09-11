package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Comment;
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
import static org.hamcrest.Matchers.*;

/**
 * COM-POST-01, COM-POST-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("POST /comments")
class CreateCommentTest extends BaseTest {

    @Test
    @DisplayName("COM-POST-01: POST /comments creates a new comment and echoes the submitted fields")
    @Story("Create a new comment")
    @Severity(SeverityLevel.CRITICAL)
    void createComment_returnsCreatedComment() {
        Comment newComment = TestDataLoader.load("new-comment.json", Comment.class);

        Comment created = given()
            .contentType(ContentType.JSON)
            .body(newComment)
        .when()
            .post("/comments")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/comment-schema.json"))
        .extract()
            .as(Comment.class);

        assertThat(created.id(), notNullValue());
        assertThat(created.postId(), equalTo(newComment.postId()));
        assertThat(created.name(), equalTo(newComment.name()));
        assertThat(created.email(), equalTo(newComment.email()));
        assertThat(created.body(), equalTo(newComment.body()));
    }

    @Test
    @DisplayName("COM-POST-02: POST /comments with an empty body still returns 201 with a generated id "
        + "(documented behavior of the fake API, which does not validate payloads)")
    @Story("Create with an empty body")
    @Severity(SeverityLevel.NORMAL)
    void createComment_emptyBody_stillReturnsCreatedWithGeneratedId() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/comments")
        .then()
            .statusCode(201)
            .header("Location", containsString("/comments/"))
            .body("id", notNullValue());
    }
}
