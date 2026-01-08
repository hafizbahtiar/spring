package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when an experience is not found.
 */
public class ExperienceNotFoundException extends RuntimeException {

    public ExperienceNotFoundException(String message) {
        super(message);
    }

    public static ExperienceNotFoundException byId(Long experienceId) {
        return new ExperienceNotFoundException("Experience not found with ID: " + experienceId);
    }

    public static ExperienceNotFoundException byIdAndUser(Long experienceId, Long userId) {
        return new ExperienceNotFoundException(
                "Experience not found with ID: " + experienceId + " for user ID: " + userId);
    }
}
