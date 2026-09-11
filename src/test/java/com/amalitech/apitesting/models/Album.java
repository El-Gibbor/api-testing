package com.amalitech.apitesting.models;

/**
 * Represents an {@code /albums} resource on the JSONPlaceholder API.
 */
public record Album(Integer userId, Integer id, String title) {
}
