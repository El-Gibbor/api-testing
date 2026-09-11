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

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * NEST-01 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Users")
class GetUserAlbumsTest extends BaseTest {

    @Test
    @DisplayName("NEST-01: GET /users/{id}/albums returns albums belonging to that user")
    @Story("GET - Fetch nested albums for a user")
    @Severity(SeverityLevel.NORMAL)
    void getAlbumsForUser_returnsMatchingAlbums() {
        int userId = 1;

        Album[] albums = given()
            .pathParam("id", userId)
            .when()
                .get("/users/{id}/albums")
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
