package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Comment;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * COM-GET-01, COM-GET-02, COM-GET-03, COM-GET-04, COM-GET-05 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Comments")
class GetCommentsTest extends BaseTest {

    @Test
    @DisplayName("COM-GET-01: GET /comments returns all comments with a valid schema")
    @Description("Returns 200 with a non-empty array of comments, the Content-Type header set to application/json, and a body that matches the comments array JSON schema.")
    @Story("GET - List all comments")
    @Severity(SeverityLevel.CRITICAL)
    void getAllComments_returnsCommentsList() {
        Comment[] comments = given()
            .when()
                .get("/comments")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/comment-array-schema.json"))
            .extract()
                .as(Comment[].class);

        assertThat(comments.length, greaterThan(0));
        assertThat(comments[0].id(), notNullValue());
    }

    @Test
    @DisplayName("COM-GET-02: GET /comments/{id} returns the requested comment")
    @Description("Returns 200 with the requested comment's fields, validated against the comment JSON schema.")
    @Story("GET - Fetch a single comment")
    @Severity(SeverityLevel.CRITICAL)
    void getCommentById_returnsMatchingComment() {
        int commentId = 1;

        Comment comment = given()
            .pathParam("id", commentId)
            .when()
                .get("/comments/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/comment-schema.json"))
            .extract()
                .as(Comment.class);

        assertThat(comment.id(), equalTo(commentId));
        assertThat(comment.postId(), notNullValue());
        assertThat(comment.name(), not(emptyOrNullString()));
        assertThat(comment.email(), not(emptyOrNullString()));
        assertThat(comment.body(), not(emptyOrNullString()));
    }

    @Test
    @DisplayName("COM-GET-03: GET /comments/{id} for a non-existent comment returns 404")
    @Description("Returns 404 when the requested comment id does not exist.")
    @Story("GET - Fetch a non-existent comment")
    @Severity(SeverityLevel.NORMAL)
    void getCommentById_nonExistentId_returnsNotFound() {
        given()
            .pathParam("id", 999_999)
        .when()
            .get("/comments/{id}")
        .then()
            .statusCode(404);
    }

    @ParameterizedTest(name = "GET /comments/{0} returns 404")
    @ValueSource(strings = {"abc", "0", "-1"})
    @DisplayName("COM-GET-04: GET /comments/{id} returns 404 for invalid ids (non-numeric, zero, negative)")
    @Description("Returns 404 for a non-numeric, zero, and negative id alike.")
    @Story("GET - Fetch with an invalid id")
    @Severity(SeverityLevel.NORMAL)
    void getCommentById_invalidId_returnsNotFound(String invalidId) {
        given()
            .pathParam("id", invalidId)
        .when()
            .get("/comments/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("COM-GET-05: GET /comments?postId={id} returns only comments belonging to that post")
    @Description("Returns 200 with only the comments belonging to the given postId, validated against the comments array schema.")
    @Story("GET - Filter comments by query parameter")
    @Severity(SeverityLevel.NORMAL)
    void getCommentsByPostId_returnsOnlyMatchingComments() {
        int postId = 1;

        Comment[] comments = given()
            .queryParam("postId", postId)
            .when()
                .get("/comments")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/comment-array-schema.json"))
            .extract()
                .as(Comment[].class);

        assertThat(comments.length, greaterThan(0));
        assertThat(Arrays.stream(comments).allMatch(comment -> comment.postId().equals(postId)), is(true));
    }
}
