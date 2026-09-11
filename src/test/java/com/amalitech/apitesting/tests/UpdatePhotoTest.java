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
import static org.hamcrest.Matchers.equalTo;

/**
 * PHO-PUT-01, PHO-PUT-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Photos")
class UpdatePhotoTest extends BaseTest {

    @Test
    @DisplayName("PHO-PUT-01: PUT /photos/{id} fully updates an existing photo")
    @Story("PUT - Fully update an existing photo")
    @Severity(SeverityLevel.CRITICAL)
    void updatePhoto_returnsUpdatedPhoto() {
        Photo updatedPhoto = TestDataLoader.load("updated-photo.json", Photo.class);

        Photo updated = given()
            .contentType(ContentType.JSON)
            .pathParam("id", updatedPhoto.id())
            .body(updatedPhoto)
        .when()
            .put("/photos/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/photo-schema.json"))
        .extract()
            .as(Photo.class);

        assertThat(updated.id(), equalTo(updatedPhoto.id()));
        assertThat(updated.albumId(), equalTo(updatedPhoto.albumId()));
        assertThat(updated.title(), equalTo(updatedPhoto.title()));
        assertThat(updated.url(), equalTo(updatedPhoto.url()));
        assertThat(updated.thumbnailUrl(), equalTo(updatedPhoto.thumbnailUrl()));
    }

    @Test
    @DisplayName("PHO-PUT-02: PUT /photos/{id} for a non-existent photo returns a server error "
        + "(KNOWN FRAGILE: same underlying backend crash as PUT-02/PUT-03 in UpdatePostTest - not a "
        + "documented contract. If this starts failing, it likely means upstream fixed the bug - relax "
        + "this assertion rather than assuming a regression.)")
    @Story("PUT - Update a non-existent photo")
    @Severity(SeverityLevel.MINOR)
    void updatePhoto_nonExistentId_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 999_999)
            .body("{\"id\":999999,\"albumId\":1,\"title\":\"updated\",\"url\":\"https://example.com/x\","
                + "\"thumbnailUrl\":\"https://example.com/x-thumb\"}")
        .when()
            .put("/photos/{id}")
        .then()
            .statusCode(500);
    }
}
