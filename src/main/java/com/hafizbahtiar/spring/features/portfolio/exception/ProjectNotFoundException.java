package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when a project is not found.
 */
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String message) {
        super(message);
    }

    public static ProjectNotFoundException byId(Long projectId) {
        return new ProjectNotFoundException("Project not found with ID: " + projectId);
    }

    public static ProjectNotFoundException byIdAndUser(Long projectId, Long userId) {
        return new ProjectNotFoundException("Project not found with ID: " + projectId + " for user ID: " + userId);
    }
}
