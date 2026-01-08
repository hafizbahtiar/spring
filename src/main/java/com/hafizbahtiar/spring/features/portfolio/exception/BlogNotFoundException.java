package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when a blog post is not found.
 */
public class BlogNotFoundException extends RuntimeException {

    public BlogNotFoundException(String message) {
        super(message);
    }

    public static BlogNotFoundException byId(Long blogId) {
        return new BlogNotFoundException("Blog post not found with ID: " + blogId);
    }

    public static BlogNotFoundException byIdAndUser(Long blogId, Long userId) {
        return new BlogNotFoundException(
                "Blog post not found with ID: " + blogId + " for user ID: " + userId);
    }

    public static BlogNotFoundException bySlug(String slug) {
        return new BlogNotFoundException("Blog post not found with slug: " + slug);
    }
}
