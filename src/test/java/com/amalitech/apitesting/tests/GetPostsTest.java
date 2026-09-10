package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Post;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * GET-01, GET-02, GET-03, GET-05 from docs/TEST_PLAN.md.
 */
class GetPostsTest extends BaseTest {

    @Test
    @DisplayName("GET-01: GET /posts returns all posts with a valid schema")
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
    void getPostById_invalidId_returnsNotFound(String invalidId) {
        given()
            .pathParam("id", invalidId)
        .when()
            .get("/posts/{id}")
        .then()
            .statusCode(404);
    }
}
