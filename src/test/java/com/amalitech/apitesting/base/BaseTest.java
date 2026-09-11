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
        RestAssured.replaceFiltersWith(new RequestLoggingFilter(), new ResponseLoggingFilter(), new AllureRestAssured());
    }
}
