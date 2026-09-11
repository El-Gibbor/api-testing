package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Album;
import com.amalitech.apitesting.utils.TestDataLoader;
import io.qameta.allure.Description;
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
import static org.hamcrest.Matchers.equalTo;

/**
 * ALB-PUT-01, ALB-PUT-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Albums")
class UpdateAlbumTest extends BaseTest {

    @Test
    @DisplayName("ALB-PUT-01: PUT /albums/{id} fully updates an existing album")
    @Description("Returns 200 with the response reflecting the updated fields.")
    @Story("PUT - Fully update an existing album")
    @Severity(SeverityLevel.CRITICAL)
    void updateAlbum_returnsUpdatedAlbum() {
        Album updatedAlbum = TestDataLoader.load("updated-album.json", Album.class);

        Album updated = given()
            .contentType(ContentType.JSON)
            .pathParam("id", updatedAlbum.id())
            .body(updatedAlbum)
        .when()
            .put("/albums/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/album-schema.json"))
        .extract()
            .as(Album.class);

        assertThat(updated.id(), equalTo(updatedAlbum.id()));
        assertThat(updated.userId(), equalTo(updatedAlbum.userId()));
        assertThat(updated.title(), equalTo(updatedAlbum.title()));
    }

    @Test
    @DisplayName("ALB-PUT-02: PUT /albums/{id} for a non-existent album returns a server error")
    @Description("KNOWN FRAGILE: same underlying backend crash as PUT-02/PUT-03 in UpdatePostTest - not a documented contract. If this starts failing, it likely means upstream fixed the bug - relax this assertion rather than assuming a regression.")
    @Story("PUT - Update a non-existent album")
    @Severity(SeverityLevel.MINOR)
    void updateAlbum_nonExistentId_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 999_999)
            .body("{\"id\":999999,\"userId\":1,\"title\":\"updated\"}")
        .when()
            .put("/albums/{id}")
        .then()
            .statusCode(500);
    }
}
