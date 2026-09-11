package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Post;
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
 * GET-01, GET-02, GET-03, GET-05, GET-06 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Posts")
class GetPostsTest extends BaseTest {

    @Test
    @DisplayName("GET-01: GET /posts returns all posts with a valid schema")
    @Description("Returns 200 with a non-empty array of posts, the Content-Type header set to application/json, and a body that matches the posts array JSON schema.")
    @Story("GET - List all posts")
    @Severity(SeverityLevel.CRITICAL)
    void getAllPosts_returnsPostsList() {
        Post[] posts = given()
            .when()
                .get("/posts")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/post-array-schema.json"))
            .extract()
                .as(Post[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(posts[0].id(), notNullValue());
    }

    @Test
    @DisplayName("GET-02: GET /posts/{id} returns the requested post")
    @Description("Returns 200 with the requested post's fields, validated against the post JSON schema.")
    @Story("GET - Fetch a single post")
    @Severity(SeverityLevel.CRITICAL)
    void getPostById_returnsMatchingPost() {
        int postId = 1;

        Post post = given()
            .pathParam("id", postId)
            .when()
                .get("/posts/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"))
            .extract()
                .as(Post.class);

        assertThat(post.id(), equalTo(postId));
        assertThat(post.userId(), notNullValue());
        assertThat(post.title(), not(emptyOrNullString()));
        assertThat(post.body(), not(emptyOrNullString()));
    }

    @Test
    @DisplayName("GET-03: GET /posts/{id} for a non-existent post returns 404")
    @Description("Returns 404 when the requested post id does not exist.")
    @Story("GET - Fetch a non-existent post")
    @Severity(SeverityLevel.NORMAL)
    void getPostById_nonExistentId_returnsNotFound() {
        given()
            .pathParam("id", 999_999)
        .when()
            .get("/posts/{id}")
        .then()
            .statusCode(404);
    }

    @ParameterizedTest(name = "GET /posts/{0} returns 404")
    @ValueSource(strings = {"abc", "0", "-1"})
    @DisplayName("GET-05: GET /posts/{id} returns 404 for invalid ids (non-numeric, zero, negative)")
    @Description("Returns 404 for a non-numeric, zero, and negative id alike.")
    @Story("GET - Fetch with an invalid id")
    @Severity(SeverityLevel.NORMAL)
    void getPostById_invalidId_returnsNotFound(String invalidId) {
        given()
            .pathParam("id", invalidId)
        .when()
            .get("/posts/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("GET-06: GET /posts?userId={id} returns only posts belonging to that user")
    @Description("Returns 200 with only the posts belonging to the given userId, validated against the posts array schema.")
    @Story("GET - Filter posts by query parameter")
    @Severity(SeverityLevel.NORMAL)
    void getPostsByUserId_returnsOnlyMatchingPosts() {
        int userId = 1;

        Post[] posts = given()
            .queryParam("userId", userId)
            .when()
                .get("/posts")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/post-array-schema.json"))
            .extract()
                .as(Post[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(Arrays.stream(posts).allMatch(post -> post.userId().equals(userId)), is(true));
    }
}
