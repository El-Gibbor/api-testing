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
 * COM-POST-01, COM-POST-02, COM-POST-03, COM-POST-04 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Comments")
class CreateCommentTest extends BaseTest {

    @Test
    @DisplayName("COM-POST-01: POST /comments creates a new comment and echoes the submitted fields")
    @Description("201, response echoes submitted fields, generated id present.")
    @Story("POST - Create a new comment")
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
    @Description("Documented actual behavior of the fake API.")
    @Story("POST - Create with an empty body")
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

    @Test
    @DisplayName("COM-POST-03: POST /comments with a non-JSON Content-Type is not parsed as JSON "
        + "(documented behavior: the fake API silently misinterprets the raw body instead of rejecting it)")
    @Description("201, but the raw body is not parsed as JSON (documented quirk).")
    @Story("POST - Create with a non-JSON Content-Type")
    @Severity(SeverityLevel.NORMAL)
    void createComment_nonJsonContentType_bodyIsNotParsedAsJson() {
        // REST Assured appends a default charset (ISO-8859-1) to the Content-Type header unless
        // told not to; that charset alone triggers a different crash (UnsupportedMediaTypeError)
        // than the one this test targets, so it must be disabled here.
        given()
            .config(RestAssuredConfig.config().encoderConfig(
                EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
            .contentType("application/x-www-form-urlencoded")
            .body("{\"name\":\"x\"}")
        .when()
            .post("/comments")
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("COM-POST-04: POST /comments with malformed JSON returns an unhandled server error "
        + "(KNOWN FRAGILE: pins to the same body-parser/json-server crash as POST-04 in CreatePostTest, "
        + "not a documented 400 contract. If this test starts failing, it likely means upstream added "
        + "input validation - relax this assertion rather than assuming a regression.)")
    @Description("500 (known fragile - pins to an upstream json-server bug, not a documented contract).")
    @Story("POST - Create with malformed JSON")
    @Severity(SeverityLevel.MINOR)
    void createComment_malformedJson_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json")
        .when()
            .post("/comments")
        .then()
            .statusCode(500);
    }
}
