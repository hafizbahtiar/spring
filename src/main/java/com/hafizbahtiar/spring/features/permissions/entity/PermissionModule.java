package com.hafizbahtiar.spring.features.permissions.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * PermissionModule entity representing a registry of available permission
 * modules.
 * This is a reference table that defines all modules in the system that can
 * have permissions.
 * Examples: "support", "finance", "portfolio", "admin"
 */
@Entity
@Table(name = "permission_modules", indexes = {
        @Index(name = "idx_permission_modules_key", columnList = "module_key")
})
@Getter
@Setter
@NoArgsConstructor
public class PermissionModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique module key (e.g., "support", "finance", "portfolio", "admin")
     */
    @Column(name = "module_key", nullable = false, length = 50, unique = true)
    private String moduleKey;

    /**
     * Human-readable module name
     */
    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    /**
     * Module description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Roles that can assign this module to groups (comma-separated)
     * Examples: "OWNER", "ADMIN", "OWNER,ADMIN"
     */
    @Column(name = "available_to_roles", nullable = false, length = 50)
    private String availableToRoles;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
