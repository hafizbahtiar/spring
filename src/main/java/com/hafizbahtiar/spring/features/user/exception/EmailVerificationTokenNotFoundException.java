package com.hafizbahtiar.spring.features.user.exception;

import com.hafizbahtiar.spring.common.exception.ValidationException;

/**
 * Exception thrown when an email verification token is not found.
 */
public class EmailVerificationTokenNotFoundException extends ValidationException {

    public EmailVerificationTokenNotFoundException(String message) {
        super(message);
    }

    public static EmailVerificationTokenNotFoundException byToken(String token) {
        return new EmailVerificationTokenNotFoundException(
                "Email verification token not found or is invalid: " + token);
    }
}
