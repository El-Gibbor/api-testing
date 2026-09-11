package com.amalitech.apitesting.models;

/**
 * Represents a {@code /photos} resource on the JSONPlaceholder API.
 */
public record Photo(Integer albumId, Integer id, String title, String url, String thumbnailUrl) {
}
