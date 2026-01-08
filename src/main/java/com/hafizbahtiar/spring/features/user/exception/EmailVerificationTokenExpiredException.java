package com.hafizbahtiar.spring.features.user.exception;

import com.hafizbahtiar.spring.common.exception.ValidationException;

/**
 * Exception thrown when an email verification token has expired.
 */
public class EmailVerificationTokenExpiredException extends ValidationException {

    public EmailVerificationTokenExpiredException(String message) {
        super(message);
    }

    public static EmailVerificationTokenExpiredException defaultMessage() {
        return new EmailVerificationTokenExpiredException("Email verification token has expired.");
    }
}
