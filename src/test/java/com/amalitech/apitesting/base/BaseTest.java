package com.amalitech.apitesting.base;

import com.amalitech.apitesting.utils.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.BeforeAll;

/**
 * Shared REST Assured configuration for all test classes.
 * The base URI is resolved via {@link ConfigReader} (system property, env var,
 * or config.properties) rather than hardcoded, so the suite can target different
 * environments without code changes.
 */
public abstract class BaseTest {

    @BeforeAll
    @SuppressWarnings("unused") // invoked reflectively by JUnit, not called directly from source
    static void setUpBaseConfig() {
        RestAssured.baseURI = ConfigReader.getBaseUri();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }
}
