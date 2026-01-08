package com.hafizbahtiar.spring.features.permissions.exception;

/**
 * Exception thrown when a permission group is not found.
 */
public class PermissionGroupNotFoundException extends RuntimeException {

    public PermissionGroupNotFoundException(String message) {
        super(message);
    }

    public static PermissionGroupNotFoundException byId(Long id) {
        return new PermissionGroupNotFoundException("Permission group not found with ID: " + id);
    }

    public static PermissionGroupNotFoundException byName(String name) {
        return new PermissionGroupNotFoundException("Permission group not found with name: " + name);
    }
}
