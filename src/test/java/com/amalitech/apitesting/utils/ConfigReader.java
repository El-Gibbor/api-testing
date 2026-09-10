package com.amalitech.apitesting.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Resolves environment-specific test configuration.
 * <p>
 * Resolution order for each key (highest priority first):
 * <ol>
 *     <li>JVM system property, e.g. {@code -Dbase.uri=https://...}</li>
 *     <li>Environment variable, e.g. {@code BASE_URI=https://...}
 *     (property name upper-cased, dots replaced with underscores)</li>
 *     <li>{@code config.properties} on the test classpath</li>
 * </ol>
 * This lets the same test suite target different environments (local, CI, Docker)
 * without any code changes.
 */
public final class ConfigReader {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = load();

    private ConfigReader() {
    }

    public static String getBaseUri() {
        return get("base.uri");
    }

    public static String get(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String envVar = System.getenv(key.toUpperCase().replace('.', '_'));
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }

        String fileValue = PROPERTIES.getProperty(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }

        throw new IllegalStateException("Missing required configuration property: " + key);
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
        return properties;
    }
}
