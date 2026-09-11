package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Album;
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
 * ALB-POST-01, ALB-POST-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("POST /albums")
class CreateAlbumTest extends BaseTest {

    @Test
    @DisplayName("ALB-POST-01: POST /albums creates a new album and echoes the submitted fields")
    @Story("Create a new album")
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
    @Story("Create with an empty body")
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
}
