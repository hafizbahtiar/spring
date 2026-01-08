package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when an education is not found.
 */
public class EducationNotFoundException extends RuntimeException {

    public EducationNotFoundException(String message) {
        super(message);
    }

    public static EducationNotFoundException byId(Long educationId) {
        return new EducationNotFoundException("Education not found with ID: " + educationId);
    }

    public static EducationNotFoundException byIdAndUser(Long educationId, Long userId) {
        return new EducationNotFoundException(
                "Education not found with ID: " + educationId + " for user ID: " + userId);
    }
}
