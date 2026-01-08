package com.hafizbahtiar.spring.features.permissions.exception;

/**
 * Exception thrown when a permission component is not found.
 */
public class PermissionComponentNotFoundException extends RuntimeException {

    public PermissionComponentNotFoundException(String message) {
        super(message);
    }

    public static PermissionComponentNotFoundException byId(Long id) {
        return new PermissionComponentNotFoundException("Permission component not found with ID: " + id);
    }

    public static PermissionComponentNotFoundException byKey(String pageKey, String componentKey) {
        return new PermissionComponentNotFoundException(
                "Permission component not found with page key: " + pageKey + " and component key: " + componentKey);
    }
}
