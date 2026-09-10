package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Post;
import com.amalitech.apitesting.utils.TestDataLoader;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * POST-01, POST-02 from docs/TEST_PLAN.md.
 */
class CreatePostTest extends BaseTest {

    @Test
    @DisplayName("POST-01: POST /posts creates a new post and echoes the submitted fields")
    void createPost_returnsCreatedPost() {
        Post newPost = TestDataLoader.load("new-post.json", Post.class);

        Post created = given()
            .contentType(ContentType.JSON)
            .body(newPost)
        .when()
            .post("/posts")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"))
        .extract()
            .as(Post.class);

        assertThat(created.id(), notNullValue());
        assertThat(created.userId(), equalTo(newPost.userId()));
        assertThat(created.title(), equalTo(newPost.title()));
        assertThat(created.body(), equalTo(newPost.body()));
    }

    @Test
    @DisplayName("POST-02: POST /posts with an empty body still returns 201 with a generated id "
        + "(documented behavior of the fake API, which does not validate payloads)")
    void createPost_emptyBody_stillReturnsCreatedWithGeneratedId() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/posts")
        .then()
            .statusCode(201)
            .header("Location", containsString("/posts/"))
            .body("id", notNullValue());
    }
}
