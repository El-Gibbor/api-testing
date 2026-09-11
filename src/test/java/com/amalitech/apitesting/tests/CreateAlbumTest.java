package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Album;
import com.amalitech.apitesting.utils.TestDataLoader;
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
 * ALB-POST-01, ALB-POST-02, ALB-POST-03, ALB-POST-04 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Albums")
class CreateAlbumTest extends BaseTest {

    @Test
    @DisplayName("ALB-POST-01: POST /albums creates a new album and echoes the submitted fields")
    @Story("POST - Create a new album")
    @Severity(SeverityLevel.CRITICAL)
    void createAlbum_returnsCreatedAlbum() {
        Album newAlbum = TestDataLoader.load("new-album.json", Album.class);

        Album created = given()
            .contentType(ContentType.JSON)
            .body(newAlbum)
        .when()
            .post("/albums")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/album-schema.json"))
        .extract()
            .as(Album.class);

        assertThat(created.id(), notNullValue());
        assertThat(created.userId(), equalTo(newAlbum.userId()));
        assertThat(created.title(), equalTo(newAlbum.title()));
    }

    @Test
    @DisplayName("ALB-POST-02: POST /albums with an empty body still returns 201 with a generated id "
        + "(documented behavior of the fake API, which does not validate payloads)")
    @Story("POST - Create with an empty body")
    @Severity(SeverityLevel.NORMAL)
    void createAlbum_emptyBody_stillReturnsCreatedWithGeneratedId() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/albums")
        .then()
            .statusCode(201)
            .header("Location", containsString("/albums/"))
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("ALB-POST-03: POST /albums with a non-JSON Content-Type is not parsed as JSON "
        + "(documented behavior: the fake API silently misinterprets the raw body instead of rejecting it)")
    @Story("POST - Create with a non-JSON Content-Type")
    @Severity(SeverityLevel.NORMAL)
    void createAlbum_nonJsonContentType_bodyIsNotParsedAsJson() {
        // REST Assured appends a default charset (ISO-8859-1) to the Content-Type header unless
        // told not to; that charset alone triggers a different crash (UnsupportedMediaTypeError)
        // than the one this test targets, so it must be disabled here.
        given()
            .config(RestAssuredConfig.config().encoderConfig(
                EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
            .contentType("application/x-www-form-urlencoded")
            .body("{\"title\":\"x\"}")
        .when()
            .post("/albums")
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("ALB-POST-04: POST /albums with malformed JSON returns an unhandled server error "
        + "(KNOWN FRAGILE: pins to the same body-parser/json-server crash as POST-04 in CreatePostTest, "
        + "not a documented 400 contract. If this test starts failing, it likely means upstream added "
        + "input validation - relax this assertion rather than assuming a regression.)")
    @Story("POST - Create with malformed JSON")
    @Severity(SeverityLevel.MINOR)
    void createAlbum_malformedJson_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json")
        .when()
            .post("/albums")
        .then()
            .statusCode(500);
    }
}
