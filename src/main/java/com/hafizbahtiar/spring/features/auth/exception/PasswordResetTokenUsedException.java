package com.hafizbahtiar.spring.features.auth.exception;

/**
 * Exception thrown when password reset token has already been used.
 */
public class PasswordResetTokenUsedException extends RuntimeException {

    public PasswordResetTokenUsedException(String message) {
        super(message);
    }

    public PasswordResetTokenUsedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PasswordResetTokenUsedException defaultMessage() {
        return new PasswordResetTokenUsedException("Password reset token has already been used");
    }
}
