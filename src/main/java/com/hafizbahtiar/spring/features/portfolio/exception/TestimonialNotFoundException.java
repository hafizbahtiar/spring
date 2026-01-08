package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when a testimonial is not found.
 */
public class TestimonialNotFoundException extends RuntimeException {

    public TestimonialNotFoundException(String message) {
        super(message);
    }

    public static TestimonialNotFoundException byId(Long testimonialId) {
        return new TestimonialNotFoundException("Testimonial not found with ID: " + testimonialId);
    }

    public static TestimonialNotFoundException byIdAndUser(Long testimonialId, Long userId) {
        return new TestimonialNotFoundException(
                "Testimonial not found with ID: " + testimonialId + " for user ID: " + userId);
    }
}
