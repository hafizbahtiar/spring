package com.hafizbahtiar.spring.features.user.exception;

import com.hafizbahtiar.spring.common.exception.ValidationException;

/**
 * Exception thrown when an email verification token has already been used.
 */
public class EmailVerificationTokenUsedException extends ValidationException {

    public EmailVerificationTokenUsedException(String message) {
        super(message);
    }

    public static EmailVerificationTokenUsedException defaultMessage() {
        return new EmailVerificationTokenUsedException("Email verification token has already been used.");
    }
}
