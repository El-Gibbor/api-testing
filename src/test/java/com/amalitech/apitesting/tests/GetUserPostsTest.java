package com.amalitech.apitesting.tests;

import com.amalitech.apitesting.base.BaseTest;
import com.amalitech.apitesting.models.Post;
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
 * NEST-03 from docs/TEST_PLAN.md.
 */
@Epic("JSONPlaceholder API")
@Feature("Users")
class GetUserPostsTest extends BaseTest {

    @Test
    @DisplayName("NEST-03: GET /users/{id}/posts returns posts belonging to that user")
    @Story("GET - Fetch nested posts for a user")
    @Severity(SeverityLevel.NORMAL)
    void getPostsForUser_returnsMatchingPosts() {
        int userId = 1;

        Post[] posts = given()
            .pathParam("id", userId)
            .when()
                .get("/users/{id}/posts")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/post-array-schema.json"))
            .extract()
                .as(Post[].class);

        assertThat(posts.length, greaterThan(0));
        assertThat(Arrays.stream(posts).allMatch(post -> post.userId().equals(userId)), is(true));
    }
}
