package com.hafizbahtiar.spring.features.navigation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * NavigationMenuItem entity for storing dynamic navigation menu items.
 * Menu items are stored in the database and can be managed dynamically
 * based on user roles and permissions.
 * 
 * <p>
 * Supports hierarchical menu structure with maximum 3 levels:
 * <ul>
 * <li>Level 0: Root items (no parent or parent_id = 0)</li>
 * <li>Level 1: Child items of root items</li>
 * <li>Level 2: Child items of level 1 items (maximum depth)</li>
 * </ul>
 */
@Entity
@Table(name = "navigation_menu_items", indexes = {
        @Index(name = "idx_nav_menu_group_label", columnList = "group_label"),
        @Index(name = "idx_nav_menu_display_order", columnList = "display_order"),
        @Index(name = "idx_nav_menu_required_role", columnList = "required_role"),
        @Index(name = "idx_nav_menu_active", columnList = "active"),
        @Index(name = "idx_nav_menu_parent_id", columnList = "parent_id"),
        @Index(name = "idx_nav_menu_level", columnList = "level"),
        @Index(name = "idx_nav_menu_group_order", columnList = "group_label, display_order"),
        @Index(name = "idx_nav_menu_role_active", columnList = "required_role, active"),
        @Index(name = "idx_nav_menu_parent_level", columnList = "parent_id, level")
})
@Getter
@Setter
@NoArgsConstructor
public class NavigationMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Menu item title (display text)
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * Menu item URL/route path
     */
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    /**
     * Icon name/identifier (e.g., "home", "briefcase", "settings")
     * Maps to Lucide React icon components in frontend
     */
    @Column(name = "icon_name", nullable = false, length = 50)
    private String iconName;

    /**
     * Group label for organizing menu items (e.g., "Navigation", "Portfolio",
     * "Admin")
     */
    @Column(name = "group_label", nullable = false, length = 50)
    private String groupLabel;

    /**
     * Display order within the group (lower numbers appear first)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * Parent menu item (self-referencing relationship).
     * NULL or parent_id = 0 for root items (level 0).
     * Maximum depth is 3 levels (0, 1, 2).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private NavigationMenuItem parent;

    /**
     * Child menu items (for convenience in queries).
     * This is the inverse side of the parent-child relationship.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NavigationMenuItem> children = new ArrayList<>();

    /**
     * Menu item level/depth in the hierarchy.
     * - Level 0: Root items (no parent)
     * - Level 1: Direct children of root items
     * - Level 2: Children of level 1 items (maximum depth)
     */
    @Column(name = "level", nullable = false)
    private Integer level = 0;

    /**
     * Required role to see this menu item (OWNER, ADMIN, USER, or NULL for all
     * authenticated users)
     */
    @Column(name = "required_role", length = 20)
    private String requiredRole;

    /**
     * Required permission module key (optional - for Layer 2 permission system)
     * If set, user must have READ access to this module to see the menu item
     */
    @Column(name = "required_permission_module", length = 50)
    private String requiredPermissionModule;

    /**
     * Required permission page key (optional - for Layer 2 permission system)
     * If set, user must have READ access to this page to see the menu item
     */
    @Column(name = "required_permission_page", length = 100)
    private String requiredPermissionPage;

    /**
     * Badge count (optional - for showing notification counts)
     */
    @Column(name = "badge")
    private Integer badge;

    /**
     * Whether this menu item is active/enabled
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Timestamp when menu item was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when menu item was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Business method to check if menu item is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Business method to deactivate menu item
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Business method to activate menu item
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Business method to check if this is a root item (level 0, no parent)
     */
    public boolean isRoot() {
        return this.level == 0 && (this.parent == null || this.parent.getId() == null || this.parent.getId() == 0);
    }

    /**
     * Business method to check if this item has children
     */
    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

    /**
     * Business method to get the parent ID (returns 0 if root item).
     * Safely accesses parent ID. May trigger lazy loading if parent is not
     * initialized.
     */
    public Long getParentId() {
        if (this.parent == null) {
            return 0L;
        }
        // Access parent ID - if parent is a proxy, this will trigger lazy loading
        // This is acceptable since we need the parent ID for hierarchy building
        return this.parent.getId() != null ? this.parent.getId() : 0L;
    }

    /**
     * Business method to set parent and update level automatically.
     * Validates that maximum depth (level 2) is not exceeded.
     *
     * @param parent Parent menu item (null for root items)
     * @throws IllegalArgumentException if setting parent would exceed maximum depth
     */
    public void setParent(NavigationMenuItem parent) {
        if (parent == null || parent.getId() == null || parent.getId() == 0) {
            this.parent = null;
            this.level = 0;
        } else {
            int parentLevel = parent.getLevel() != null ? parent.getLevel() : 0;
            if (parentLevel >= 2) {
                throw new IllegalArgumentException(
                        "Cannot add child to menu item at level " + parentLevel
                                + ". Maximum depth is 3 levels (0, 1, 2).");
            }
            this.parent = parent;
            this.level = parentLevel + 1;
        }
    }
}
