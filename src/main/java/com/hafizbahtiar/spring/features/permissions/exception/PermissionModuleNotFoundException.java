package com.hafizbahtiar.spring.features.permissions.exception;

/**
 * Exception thrown when a permission module is not found.
 */
public class PermissionModuleNotFoundException extends RuntimeException {

    public PermissionModuleNotFoundException(String message) {
        super(message);
    }

    public static PermissionModuleNotFoundException byId(Long id) {
        return new PermissionModuleNotFoundException("Permission module not found with ID: " + id);
    }

    public static PermissionModuleNotFoundException byKey(String moduleKey) {
        return new PermissionModuleNotFoundException("Permission module not found with key: " + moduleKey);
    }
}
