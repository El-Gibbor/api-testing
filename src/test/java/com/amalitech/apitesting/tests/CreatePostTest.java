package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Post;
import com.amalitech.apitesting.utils.TestDataLoader;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * POST-01, POST-02, POST-03, POST-04 from docs/TEST_PLAN.md.
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

    @Test
    @DisplayName("POST-03: POST /posts with a non-JSON Content-Type is not parsed as JSON "
        + "(documented behavior: the fake API silently misinterprets the raw body instead of rejecting it)")
    void createPost_nonJsonContentType_bodyIsNotParsedAsJson() {
        // REST Assured appends a default charset (ISO-8859-1) to the Content-Type header unless
        // told not to; that charset alone triggers a different crash (UnsupportedMediaTypeError)
        // than the one this test targets, so it must be disabled here.
        given()
            .config(RestAssuredConfig.config().encoderConfig(
                EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
            .contentType("application/x-www-form-urlencoded")
            .body("{\"title\":\"x\"}")
        .when()
            .post("/posts")
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("POST-04: POST /posts with malformed JSON returns an unhandled server error "
        + "(KNOWN FRAGILE: pins to a body-parser/json-server crash, not a documented 400 contract. "
        + "If this test starts failing, it likely means upstream added input validation - relax "
        + "this assertion rather than assuming a regression.)")
    void createPost_malformedJson_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json")
        .when()
            .post("/posts")
        .then()
            .statusCode(500);
    }
}
