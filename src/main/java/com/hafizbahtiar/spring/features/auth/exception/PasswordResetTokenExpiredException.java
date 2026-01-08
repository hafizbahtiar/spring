package com.hafizbahtiar.spring.features.auth.exception;

/**
 * Exception thrown when password reset token has expired.
 */
public class PasswordResetTokenExpiredException extends RuntimeException {

    public PasswordResetTokenExpiredException(String message) {
        super(message);
    }

    public PasswordResetTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PasswordResetTokenExpiredException defaultMessage() {
        return new PasswordResetTokenExpiredException("Password reset token has expired");
    }
}
