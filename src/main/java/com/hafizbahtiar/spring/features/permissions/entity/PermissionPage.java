package com.hafizbahtiar.spring.features.permissions.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * PermissionPage entity representing a registry of available pages within
 * modules.
 * This is a reference table that defines all pages in the system that can have
 * permissions.
 * Examples: "support.chat", "support.tickets", "finance.dashboard"
 */
@Entity
@Table(name = "permission_pages", indexes = {
        @Index(name = "idx_permission_pages_module", columnList = "module_key")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_page_unique", columnNames = { "module_key", "page_key" })
})
@Getter
@Setter
@NoArgsConstructor
public class PermissionPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Module key this page belongs to (references PermissionModule.moduleKey)
     */
    @Column(name = "module_key", nullable = false, length = 50)
    private String moduleKey;

    /**
     * Unique page key within the module (e.g., "chat", "tickets", "dashboard")
     */
    @Column(name = "page_key", nullable = false, length = 100)
    private String pageKey;

    /**
     * Human-readable page name
     */
    @Column(name = "page_name", nullable = false, length = 200)
    private String pageName;

    /**
     * Route path for this page (e.g., "/support/chat", "/support/tickets")
     */
    @Column(name = "route_path", length = 500)
    private String routePath;

    /**
     * Page description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
