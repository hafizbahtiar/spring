package com.hafizbahtiar.spring.features.navigation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a navigation menu item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuItemRequest {

    /**
     * Menu item title (display text)
     */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    /**
     * Menu item URL/route path
     */
    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL must not exceed 500 characters")
    private String url;

    /**
     * Icon name/identifier (e.g., "home", "briefcase", "settings")
     */
    @NotBlank(message = "Icon name is required")
    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    private String iconName;

    /**
     * Group label for organizing menu items (e.g., "Navigation", "Portfolio",
     * "Admin")
     */
    @NotBlank(message = "Group label is required")
    @Size(max = 50, message = "Group label must not exceed 50 characters")
    private String groupLabel;

    /**
     * Display order within the group (lower numbers appear first)
     */
    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    /**
     * Parent menu item ID (optional - NULL or 0 for root items)
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
     * Whether this menu item is active/enabled (default: true)
     */
    private Boolean active = true;
}
