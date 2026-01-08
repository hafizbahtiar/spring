package com.hafizbahtiar.spring.features.permissions.exception;

/**
 * General exception for permission-related errors.
 */
public class PermissionException extends RuntimeException {

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PermissionException groupNameAlreadyExists(String name) {
        return new PermissionException("Permission group with name already exists: " + name);
    }

    public static PermissionException userAlreadyInGroup(Long userId, Long groupId) {
        return new PermissionException("User with ID: " + userId + " is already assigned to group ID: " + groupId);
    }

    public static PermissionException userNotInGroup(Long userId, Long groupId) {
        return new PermissionException("User with ID: " + userId + " is not assigned to group ID: " + groupId);
    }

    public static PermissionException invalidPermission(String message) {
        return new PermissionException("Invalid permission: " + message);
    }

    public static PermissionException creatorAccessViolation(String message) {
        return new PermissionException("Creator access violation: " + message);
    }

    public static PermissionException moduleKeyAlreadyExists(String moduleKey) {
        return new PermissionException("Permission module with key already exists: " + moduleKey);
    }

    public static PermissionException pageKeyAlreadyExists(String moduleKey, String pageKey) {
        return new PermissionException(
                "Permission page with key already exists in module: " + moduleKey + "." + pageKey);
    }

    public static PermissionException componentKeyAlreadyExists(String pageKey, String componentKey) {
        return new PermissionException(
                "Permission component with key already exists in page: " + pageKey + "." + componentKey);
    }
}
