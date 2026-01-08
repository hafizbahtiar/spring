package com.hafizbahtiar.spring.features.user.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UserAlreadyExistsException email(String email) {
        return new UserAlreadyExistsException("Email already exists: " + email);
    }

    public static UserAlreadyExistsException username(String username) {
        return new UserAlreadyExistsException("Username already exists: " + username);
    }
}
