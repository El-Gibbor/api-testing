package com.amalitech.apitesting.models;

/**
 * Represents a {@code /posts} resource on the JSONPlaceholder API.
 */
public record Post(Integer id, Integer userId, String title, String body) {
}
