package com.hafizbahtiar.spring.features.navigation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a navigation menu item.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuItemRequest {

    /**
     * Menu item title (display text)
     */
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    /**
     * Menu item URL/route path
     */
    @Size(max = 500, message = "URL must not exceed 500 characters")
    private String url;

    /**
     * Icon name/identifier (e.g., "home", "briefcase", "settings")
     */
    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    private String iconName;

    /**
     * Group label for organizing menu items (e.g., "Navigation", "Portfolio",
     * "Admin")
     */
    @Size(max = 50, message = "Group label must not exceed 50 characters")
    private String groupLabel;

    /**
     * Display order within the group (lower numbers appear first)
     */
    private Integer displayOrder;

    /**
     * Parent menu item ID (NULL or 0 for root items)
     */
    private Long parentId;

    /**
     * Required role to see this menu item (OWNER, ADMIN, USER, or NULL for all
     * authenticated users)
     */
    @Size(max = 20, message = "Required role must not exceed 20 characters")
    private String requiredRole;

    /**
     * Required permission module key (optional - for Layer 2 permission system)
     */
    @Size(max = 50, message = "Required permission module must not exceed 50 characters")
    private String requiredPermissionModule;

    /**
     * Required permission page key (optional - for Layer 2 permission system)
     */
    @Size(max = 100, message = "Required permission page must not exceed 100 characters")
    private String requiredPermissionPage;

    /**
     * Badge count (optional - for showing notification counts)
     */
    private Integer badge;

    /**
     * Whether this menu item is active/enabled
     */
    private Boolean active;
}
