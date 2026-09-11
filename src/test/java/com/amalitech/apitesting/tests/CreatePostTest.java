package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Post;
import com.amalitech.apitesting.utils.TestDataLoader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
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
@Epic("JSONPlaceholder API")
@Feature("Posts")
class CreatePostTest extends BaseTest {

    @Test
    @DisplayName("POST-01: POST /posts creates a new post and echoes the submitted fields")
    @Description("Returns 201 with the created post echoing the submitted fields and a generated id.")
    @Story("POST - Create a new post")
    @Severity(SeverityLevel.CRITICAL)
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
    @DisplayName("POST-02: POST /posts with an empty body still succeeds")
    @Description("Documented behavior of the fake API: it does not validate payloads, so an empty body still returns 201 with a generated id.")
    @Story("POST - Create with an empty body")
    @Severity(SeverityLevel.NORMAL)
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
    @DisplayName("POST-03: POST /posts with a non-JSON Content-Type is not parsed as JSON")
    @Description("Documented quirk: the fake API silently misinterprets the raw body instead of rejecting it, still returning 201.")
    @Story("POST - Create with a non-JSON Content-Type")
    @Severity(SeverityLevel.NORMAL)
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
    @DisplayName("POST-04: POST /posts with malformed JSON returns an unhandled server error")
    @Description("KNOWN FRAGILE: pins to a body-parser/json-server crash, not a documented 400 contract. If this starts failing, it likely means upstream added input validation - relax this assertion rather than assuming a regression.")
    @Story("POST - Create with malformed JSON")
    @Severity(SeverityLevel.MINOR)
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
