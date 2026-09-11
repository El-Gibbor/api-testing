package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Photo;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * NEST-04 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Albums")
class GetAlbumPhotosTest extends BaseTest {

    @Test
    @DisplayName("NEST-04: GET /albums/{id}/photos returns photos belonging to that album")
    @Description("200, all items reference the parent albumId, schema.")
    @Story("GET - Fetch nested photos for an album")
    @Severity(SeverityLevel.NORMAL)
    void getPhotosForAlbum_returnsMatchingPhotos() {
        int albumId = 1;

        Photo[] photos = given()
            .pathParam("id", albumId)
            .when()
                .get("/albums/{id}/photos")
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
