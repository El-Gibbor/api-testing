package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Photo;
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
 * PHO-GET-01, PHO-GET-02, PHO-GET-03, PHO-GET-04, PHO-GET-05 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Photos")
class GetPhotosTest extends BaseTest {

    @Test
    @DisplayName("PHO-GET-01: GET /photos returns all photos with a valid schema")
    @Story("GET - List all photos")
    @Severity(SeverityLevel.CRITICAL)
    void getAllPhotos_returnsPhotosList() {
        Photo[] photos = given()
            .when()
                .get("/photos")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/photo-array-schema.json"))
            .extract()
                .as(Photo[].class);

        assertThat(photos.length, greaterThan(0));
        assertThat(photos[0].id(), notNullValue());
    }

    @Test
    @DisplayName("PHO-GET-02: GET /photos/{id} returns the requested photo")
    @Story("GET - Fetch a single photo")
    @Severity(SeverityLevel.CRITICAL)
    void getPhotoById_returnsMatchingPhoto() {
        int photoId = 1;

        Photo photo = given()
            .pathParam("id", photoId)
            .when()
                .get("/photos/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/photo-schema.json"))
            .extract()
                .as(Photo.class);

        assertThat(photo.id(), equalTo(photoId));
        assertThat(photo.albumId(), notNullValue());
        assertThat(photo.title(), not(emptyOrNullString()));
        assertThat(photo.url(), not(emptyOrNullString()));
        assertThat(photo.thumbnailUrl(), not(emptyOrNullString()));
    }

    @Test
    @DisplayName("PHO-GET-03: GET /photos/{id} for a non-existent photo returns 404")
    @Story("GET - Fetch a non-existent photo")
    @Severity(SeverityLevel.NORMAL)
    void getPhotoById_nonExistentId_returnsNotFound() {
        given()
            .pathParam("id", 999_999)
        .when()
            .get("/photos/{id}")
        .then()
            .statusCode(404);
    }

    @ParameterizedTest(name = "GET /photos/{0} returns 404")
    @ValueSource(strings = {"abc", "0", "-1"})
    @DisplayName("PHO-GET-04: GET /photos/{id} returns 404 for invalid ids (non-numeric, zero, negative)")
    @Story("GET - Fetch with an invalid id")
    @Severity(SeverityLevel.NORMAL)
    void getPhotoById_invalidId_returnsNotFound(String invalidId) {
        given()
            .pathParam("id", invalidId)
        .when()
            .get("/photos/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("PHO-GET-05: GET /photos?albumId={id} returns only photos belonging to that album")
    @Story("GET - Filter photos by query parameter")
    @Severity(SeverityLevel.NORMAL)
    void getPhotosByAlbumId_returnsOnlyMatchingPhotos() {
        int albumId = 1;

        Photo[] photos = given()
            .queryParam("albumId", albumId)
            .when()
                .get("/photos")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/photo-array-schema.json"))
            .extract()
                .as(Photo[].class);

        assertThat(photos.length, greaterThan(0));
        assertThat(Arrays.stream(photos).allMatch(photo -> photo.albumId().equals(albumId)), is(true));
    }
}
