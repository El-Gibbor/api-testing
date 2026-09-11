package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.User;
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
 * USR-PUT-01, USR-PUT-02 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Users")
class UpdateUserTest extends BaseTest {

    @Test
    @DisplayName("USR-PUT-01: PUT /users/{id} fully updates an existing user")
    @Description("200, response reflects updated fields.")
    @Story("PUT - Fully update an existing user")
    @Severity(SeverityLevel.CRITICAL)
    void updateUser_returnsUpdatedUser() {
        User updatedUser = TestDataLoader.load("updated-user.json", User.class);

        User updated = given()
            .contentType(ContentType.JSON)
            .pathParam("id", updatedUser.id())
            .body(updatedUser)
        .when()
            .put("/users/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"))
        .extract()
            .as(User.class);

        assertThat(updated.id(), equalTo(updatedUser.id()));
        assertThat(updated.name(), equalTo(updatedUser.name()));
        assertThat(updated.username(), equalTo(updatedUser.username()));
        assertThat(updated.email(), equalTo(updatedUser.email()));
        assertThat(updated.address(), equalTo(updatedUser.address()));
        assertThat(updated.company(), equalTo(updatedUser.company()));
    }

    @Test
    @DisplayName("USR-PUT-02: PUT /users/{id} for a non-existent user returns a server error "
        + "(KNOWN FRAGILE: same underlying backend crash as PUT-02/PUT-03 in UpdatePostTest - not a "
        + "documented contract. If this starts failing, it likely means upstream fixed the bug - relax "
        + "this assertion rather than assuming a regression.)")
    @Description("500 (known fragile - pins to an upstream json-server bug, not a documented contract).")
    @Story("PUT - Update a non-existent user")
    @Severity(SeverityLevel.MINOR)
    void updateUser_nonExistentId_returnsServerError() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 999_999)
            .body("{\"id\":999999,\"name\":\"updated\",\"username\":\"updated\","
                + "\"email\":\"updated@example.com\"}")
        .when()
            .put("/users/{id}")
        .then()
            .statusCode(500);
    }
}
