package com.hafizbahtiar.spring.features.permissions.entity;

/**
 * Enum representing the action that can be performed with a permission.
 * Defines what operations are allowed on a resource.
 */
public enum PermissionAction {
    /**
     * Read/view access.
     * Allows viewing and reading data, but not modifying it.
     */
    READ,

    /**
     * Write access.
     * Allows creating and updating data.
     * Implies READ access.
     */
    WRITE,

    /**
     * Delete access.
     * Allows deleting data.
     * Implies READ access.
     */
    DELETE,

    /**
     * Execute/run access.
     * Allows executing operations like exporting data, running reports, triggering
     * actions.
     * Does not imply READ/WRITE/DELETE access.
     */
    EXECUTE
}
