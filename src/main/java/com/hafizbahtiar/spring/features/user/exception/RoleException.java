package com.hafizbahtiar.spring.features.user.exception;

import com.hafizbahtiar.spring.common.exception.ValidationException;

/**
 * Exception for role-related validation errors.
 */
public class RoleException extends ValidationException {

    public RoleException(String message) {
        super(message);
    }

    /**
     * Thrown when trying to assign OWNER role but one already exists.
     */
    public static RoleException ownerAlreadyExists() {
        return new RoleException("OWNER role already exists. Only one user can have the OWNER role at a time.");
    }

    /**
     * Thrown when trying to remove OWNER role from the only owner.
     */
    public static RoleException cannotRemoveOwner() {
        return new RoleException("Cannot remove OWNER role. At least one user must have the OWNER role.");
    }

    /**
     * Thrown when trying to assign an invalid role.
     */
    public static RoleException invalidRole(String role) {
        return new RoleException("Invalid role: " + role + ". Valid roles are: USER, OWNER, ADMIN");
    }
}

