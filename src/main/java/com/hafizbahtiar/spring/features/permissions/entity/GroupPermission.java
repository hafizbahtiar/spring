package com.hafizbahtiar.spring.features.permissions.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * GroupPermission entity representing a permission assigned to a permission
 * group.
 * Defines what resources (module/page/component) and actions
 * (READ/WRITE/DELETE/EXECUTE)
 * are granted or denied to users in the group.
 */
@Entity
@Table(name = "group_permissions", indexes = {
        @Index(name = "idx_group_permissions_group_id", columnList = "group_id"),
        @Index(name = "idx_group_permissions_resource", columnList = "resource_type, resource_identifier"),
        @Index(name = "idx_group_permissions_type_action", columnList = "permission_type, action")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_permission_unique", columnNames = { "group_id", "permission_type",
                "resource_type", "resource_identifier", "action" })
})
@Getter
@Setter
@NoArgsConstructor
public class GroupPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Permission group this permission belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PermissionGroup group;

    /**
     * Type of permission (MODULE, PAGE, or COMPONENT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, length = 20)
    private PermissionType permissionType;

    /**
     * Resource type (e.g., "support", "finance", "portfolio", "admin")
     * For MODULE: the module key
     * For PAGE: the module key
     * For COMPONENT: the module key
     */
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    /**
     * Resource identifier (e.g., "chat", "tickets", "edit_button")
     * For MODULE: empty or module key
     * For PAGE: the page key (e.g., "chat", "tickets")
     * For COMPONENT: the component key (e.g., "edit_button", "delete_button")
     */
    @Column(name = "resource_identifier", nullable = false, length = 200)
    private String resourceIdentifier;

    /**
     * Action allowed (READ, WRITE, DELETE, EXECUTE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private PermissionAction action;

    /**
     * Whether this permission is granted (true) or denied (false)
     * true = allow access
     * false = explicitly deny access (overrides allow permissions)
     */
    @Column(name = "granted", nullable = false)
    private Boolean granted = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Business method to check if permission is granted
     */
    public boolean isGranted() {
        return granted != null && granted;
    }

    /**
     * Business method to check if permission is denied
     */
    public boolean isDenied() {
        return granted != null && !granted;
    }
}
