package com.hafizbahtiar.spring.features.permissions.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * PermissionComponent entity representing a registry of available UI components
 * within pages.
 * This is a reference table that defines all components in the system that can
 * have permissions.
 * Examples: "edit_button", "delete_button", "export_button"
 */
@Entity
@Table(name = "permission_components", indexes = {
        @Index(name = "idx_permission_components_page", columnList = "page_key")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_component_unique", columnNames = { "page_key", "component_key" })
})
@Getter
@Setter
@NoArgsConstructor
public class PermissionComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Page key this component belongs to (references PermissionPage.pageKey)
     * Format: "module.page" (e.g., "support.chat", "finance.dashboard")
     */
    @Column(name = "page_key", nullable = false, length = 100)
    private String pageKey;

    /**
     * Unique component key within the page (e.g., "edit_button", "delete_button",
     * "export_button")
     */
    @Column(name = "component_key", nullable = false, length = 100)
    private String componentKey;

    /**
     * Human-readable component name
     */
    @Column(name = "component_name", nullable = false, length = 200)
    private String componentName;

    /**
     * Component type (e.g., "BUTTON", "LINK", "MENU_ITEM", "TAB")
     */
    @Column(name = "component_type", nullable = false, length = 50)
    private String componentType;

    /**
     * Component description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
