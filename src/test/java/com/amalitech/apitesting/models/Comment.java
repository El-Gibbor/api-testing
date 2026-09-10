package com.amalitech.apitesting.models;

/**
 * Represents a {@code /comments} resource on the JSONPlaceholder API,
 * nested under a post as {@code /posts/{id}/comments}.
 */
public record Comment(Integer postId, Integer id, String name, String email, String body) {
}
