package com.amalitech.apitesting.base;

import org.junit.jupiter.api.BeforeAll;

import com.amalitech.apitesting.utils.ConfigReader;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

/**
 * Shared REST Assured configuration for all test classes.
 */
public abstract class BaseTest {

    @BeforeAll
    @SuppressWarnings("unused")
    static void setUpBaseConfig() {
        RestAssured.baseURI = ConfigReader.getBaseUri();
        // RequestLoggingFilter/ResponseLoggingFilter print each call to the console for local
        // debugging; AllureRestAssured attaches that same request/response (method, URL, headers,
        // body, status) to the Allure report itself, which the other two never do on their own.
        //
        // Must be replaceFiltersWith, not filters: the filter list is static and @BeforeAll runs
        // once per test class, so filters(...) would append 3 more filters on every class instead
        // of resetting to these 3 - duplicating every log line and every Allure attachment once per
        // test class that had already run.
        RestAssured.replaceFiltersWith(new RequestLoggingFilter(), new ResponseLoggingFilter(), new AllureRestAssured());
    }
}
