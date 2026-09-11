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

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * GET-04 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Posts")
class GetPostCommentsTest extends BaseTest {

    @Test
    @DisplayName("GET-04: GET /posts/{id}/comments returns comments belonging to that post")
    @Description("Returns 200 with all comments referencing the parent postId, validated against the comments array schema.")
    @Story("GET - Fetch nested comments for a post")
    @Severity(SeverityLevel.NORMAL)
    void getCommentsForPost_returnsMatchingComments() {
        int postId = 1;

        Comment[] comments = given()
            .pathParam("id", postId)
            .when()
                .get("/posts/{id}/comments")
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
