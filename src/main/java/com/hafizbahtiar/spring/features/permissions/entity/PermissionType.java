package com.hafizbahtiar.spring.features.permissions.entity;

/**
 * Enum representing the type of permission.
 * Defines the level at which permissions are granted.
 */
public enum PermissionType {
    /**
     * Module-level permission.
     * Grants access to entire feature module (e.g., "support", "finance",
     * "portfolio").
     * Automatically grants access to all pages and components within that module.
     */
    MODULE,

    /**
     * Page-level permission.
     * Grants access to specific page within a module (e.g., "support.chat",
     * "finance.dashboard").
     * Automatically grants access to all components within that page.
     */
    PAGE,

    /**
     * Component-level permission.
     * Grants access to specific UI component (e.g., "edit_button", "delete_button",
     * "export_button").
     * Most granular level of permission.
     */
    COMPONENT
}
