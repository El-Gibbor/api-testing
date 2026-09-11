package com.amalitech.apitesting.models;

/**
 * Represents a {@code /todos} resource on the JSONPlaceholder API.
 */
public record Todo(Integer userId, Integer id, String title, Boolean completed) {
}
