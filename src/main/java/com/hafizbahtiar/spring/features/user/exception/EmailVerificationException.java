package com.hafizbahtiar.spring.features.user.exception;

import com.hafizbahtiar.spring.common.exception.ValidationException;

/**
 * General exception for email verification errors.
 */
public class EmailVerificationException extends ValidationException {

    public EmailVerificationException(String message) {
        super(message);
    }

    public static EmailVerificationException emailAlreadyVerified() {
        return new EmailVerificationException("Email is already verified.");
    }

    public static EmailVerificationException rateLimitExceeded(String message) {
        return new EmailVerificationException(message);
    }
}

