package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Photo;
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
 * PHO-POST-01, PHO-POST-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("POST /photos")
class CreatePhotoTest extends BaseTest {

    @Test
    @DisplayName("PHO-POST-01: POST /photos creates a new photo and echoes the submitted fields")
    @Story("Create a new photo")
    @Severity(SeverityLevel.CRITICAL)
    void createPhoto_returnsCreatedPhoto() {
        Photo newPhoto = TestDataLoader.load("new-photo.json", Photo.class);

        Photo created = given()
            .contentType(ContentType.JSON)
            .body(newPhoto)
        .when()
            .post("/photos")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/photo-schema.json"))
        .extract()
            .as(Photo.class);

        assertThat(created.id(), notNullValue());
        assertThat(created.albumId(), equalTo(newPhoto.albumId()));
        assertThat(created.title(), equalTo(newPhoto.title()));
        assertThat(created.url(), equalTo(newPhoto.url()));
        assertThat(created.thumbnailUrl(), equalTo(newPhoto.thumbnailUrl()));
    }

    @Test
    @DisplayName("PHO-POST-02: POST /photos with an empty body still returns 201 with a generated id "
        + "(documented behavior of the fake API, which does not validate payloads)")
    @Story("Create with an empty body")
    @Severity(SeverityLevel.NORMAL)
    void createPhoto_emptyBody_stillReturnsCreatedWithGeneratedId() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/photos")
        .then()
            .statusCode(201)
            .header("Location", containsString("/photos/"))
            .body("id", notNullValue());
    }
}
