package com.hafizbahtiar.spring.common.security;

/**
 * User roles for role-based access control (RBAC).
 * 
 * Role Definitions:
 * - OWNER: Unique role, only ONE user can have this role at a time (the primary user)
 * - ADMIN: Multiple users can have this role (assistants/help staff)
 * - USER: Default role for regular users
 */
public enum Role {
    USER("USER"),
    OWNER("OWNER"),
    ADMIN("ADMIN");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role fromString(String role) {
        for (Role r : Role.values()) {
            if (r.value.equalsIgnoreCase(role)) {
                return r;
            }
        }
        return USER; // Default to USER if not found
    }
}
