package com.amalitech.apitesting.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads JSON test data fixtures from {@code src/test/resources/testdata/} into POJOs,
 * so request/response payloads live in data files rather than being hardcoded in test methods.
 */
public final class TestDataLoader {

    private static final String TESTDATA_DIR = "testdata/";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestDataLoader() {
    }

    /**
     * Reads {@code testdata/<fileName>} from the classpath and deserializes it into the given type.
     */
    public static <T> T load(String fileName, Class<T> type) {
        String path = TESTDATA_DIR + fileName;
        try (InputStream input = TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalArgumentException("Test data file not found on classpath: " + path);
            }
            return MAPPER.readValue(input, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load test data file: " + path, e);
        }
    }
}
