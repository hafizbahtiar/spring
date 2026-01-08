package com.hafizbahtiar.spring.features.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidCredentialsException defaultMessage() {
        return new InvalidCredentialsException("Invalid credentials");
    }

    public static InvalidCredentialsException userNotFound() {
        return new InvalidCredentialsException("Invalid credentials");
    }

    public static InvalidCredentialsException passwordMismatch() {
        return new InvalidCredentialsException("Invalid credentials");
    }
}
