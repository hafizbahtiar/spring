package com.hafizbahtiar.spring.features.auth.exception;

/**
 * General exception for password reset errors.
 */
public class PasswordResetException extends RuntimeException {

    public PasswordResetException(String message) {
        super(message);
    }

    public PasswordResetException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PasswordResetException requestFailed() {
        return new PasswordResetException("Failed to process password reset request");
    }

    public static PasswordResetException resetFailed() {
        return new PasswordResetException("Failed to reset password");
    }

    public static PasswordResetException rateLimitExceeded() {
        return new PasswordResetException("Too many password reset requests. Please try again later");
    }
}
