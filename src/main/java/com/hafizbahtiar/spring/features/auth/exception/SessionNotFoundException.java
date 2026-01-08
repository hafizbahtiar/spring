package com.hafizbahtiar.spring.features.auth.exception;

/**
 * Exception thrown when a session is not found.
 */
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String message) {
        super(message);
    }

    /**
     * Create exception for session not found by session ID
     */
    public static SessionNotFoundException bySessionId(String sessionId) {
        return new SessionNotFoundException("Session not found with ID: " + sessionId);
    }

    /**
     * Create exception for session not found by ID
     */
    public static SessionNotFoundException byId(Long id) {
        return new SessionNotFoundException("Session not found with ID: " + id);
    }
}
