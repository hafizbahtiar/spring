package com.hafizbahtiar.spring.features.permissions.exception;

/**
 * Exception thrown when a permission is not found.
 */
public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(String message) {
        super(message);
    }

    public static PermissionNotFoundException byId(Long id) {
        return new PermissionNotFoundException("Permission not found with ID: " + id);
    }
}
