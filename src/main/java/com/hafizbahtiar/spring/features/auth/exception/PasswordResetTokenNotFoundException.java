package com.hafizbahtiar.spring.features.auth.exception;

/**
 * Exception thrown when password reset token is not found.
 */
public class PasswordResetTokenNotFoundException extends RuntimeException {

    public PasswordResetTokenNotFoundException(String message) {
        super(message);
    }

    public PasswordResetTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PasswordResetTokenNotFoundException byToken() {
        return new PasswordResetTokenNotFoundException("Password reset token not found");
    }

    public static PasswordResetTokenNotFoundException byToken(String token) {
        return new PasswordResetTokenNotFoundException("Password reset token not found: " + token);
    }
}
