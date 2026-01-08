package com.hafizbahtiar.spring.features.permissions.exception;

/**
 * Exception thrown when a permission page is not found.
 */
public class PermissionPageNotFoundException extends RuntimeException {

    public PermissionPageNotFoundException(String message) {
        super(message);
    }

    public static PermissionPageNotFoundException byId(Long id) {
        return new PermissionPageNotFoundException("Permission page not found with ID: " + id);
    }

    public static PermissionPageNotFoundException byKey(String moduleKey, String pageKey) {
        return new PermissionPageNotFoundException(
                "Permission page not found with module key: " + moduleKey + " and page key: " + pageKey);
    }
}
