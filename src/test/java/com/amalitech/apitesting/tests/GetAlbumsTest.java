package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Album;
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
 * ALB-GET-01, ALB-GET-02, ALB-GET-03, ALB-GET-04, ALB-GET-05 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("GET /albums")
class GetAlbumsTest extends BaseTest {

    @Test
    @DisplayName("ALB-GET-01: GET /albums returns all albums with a valid schema")
    @Story("List all albums")
    @Severity(SeverityLevel.CRITICAL)
    void getAllAlbums_returnsAlbumsList() {
        Album[] albums = given()
            .when()
                .get("/albums")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/album-array-schema.json"))
            .extract()
                .as(Album[].class);

        assertThat(albums.length, greaterThan(0));
        assertThat(albums[0].id(), notNullValue());
    }

    @Test
    @DisplayName("ALB-GET-02: GET /albums/{id} returns the requested album")
    @Story("Fetch a single album")
    @Severity(SeverityLevel.CRITICAL)
    void getAlbumById_returnsMatchingAlbum() {
        int albumId = 1;

        Album album = given()
            .pathParam("id", albumId)
            .when()
                .get("/albums/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/album-schema.json"))
            .extract()
                .as(Album.class);

        assertThat(album.id(), equalTo(albumId));
        assertThat(album.userId(), notNullValue());
        assertThat(album.title(), not(emptyOrNullString()));
    }

    @Test
    @DisplayName("ALB-GET-03: GET /albums/{id} for a non-existent album returns 404")
    @Story("Fetch a non-existent album")
    @Severity(SeverityLevel.NORMAL)
    void getAlbumById_nonExistentId_returnsNotFound() {
        given()
            .pathParam("id", 999_999)
        .when()
            .get("/albums/{id}")
        .then()
            .statusCode(404);
    }

    @ParameterizedTest(name = "GET /albums/{0} returns 404")
    @ValueSource(strings = {"abc", "0", "-1"})
    @DisplayName("ALB-GET-04: GET /albums/{id} returns 404 for invalid ids (non-numeric, zero, negative)")
    @Story("Fetch with an invalid id")
    @Severity(SeverityLevel.NORMAL)
    void getAlbumById_invalidId_returnsNotFound(String invalidId) {
        given()
            .pathParam("id", invalidId)
        .when()
            .get("/albums/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("ALB-GET-05: GET /albums?userId={id} returns only albums belonging to that user")
    @Story("Filter albums by query parameter")
    @Severity(SeverityLevel.NORMAL)
    void getAlbumsByUserId_returnsOnlyMatchingAlbums() {
        int userId = 1;

        Album[] albums = given()
            .queryParam("userId", userId)
            .when()
                .get("/albums")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/album-array-schema.json"))
            .extract()
                .as(Album[].class);

        assertThat(albums.length, greaterThan(0));
        assertThat(Arrays.stream(albums).allMatch(album -> album.userId().equals(userId)), is(true));
    }
}
