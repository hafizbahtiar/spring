package com.hafizbahtiar.spring.features.navigation.service;

import com.hafizbahtiar.spring.common.security.Role;
import com.hafizbahtiar.spring.features.navigation.dto.CreateMenuItemRequest;
import com.hafizbahtiar.spring.features.navigation.dto.MenuItemsResponse;
import com.hafizbahtiar.spring.features.navigation.dto.NavigationMenuItemResponse;
import com.hafizbahtiar.spring.features.navigation.dto.UpdateMenuItemRequest;

import java.util.List;

/**
 * Service interface for navigation menu management.
 * Handles CRUD operations, hierarchical menu structures, and permission-based
 * filtering.
 */
public interface NavigationMenuService {

    /**
     * Get menu items for a specific user based on their role and permissions.
     * Filters menu items based on:
     * - User's static role (OWNER, ADMIN, USER)
     * - User's group permissions (if Layer 2 is implemented)
     * - Menu item's required_role, required_permission_module, and
     * required_permission_page
     *
     * @param userId User ID
     * @return MenuItemsResponse with hierarchical menu structure grouped by group
     *         label
     */
    MenuItemsResponse getMenuItemsForUser(Long userId);

    /**
     * Get menu items for a specific role (for admin management).
     * Returns all menu items that are visible to users with the specified role.
     *
     * @param role User role (OWNER, ADMIN, USER)
     * @return MenuItemsResponse with hierarchical menu structure grouped by group
     *         label
     */
    MenuItemsResponse getMenuItemsForRole(Role role);

    /**
     * Create a new menu item.
     * Validates parent-child relationship and maximum depth (3 levels).
     *
     * @param request Create menu item request
     * @return Created NavigationMenuItemResponse
     * @throws IllegalArgumentException if parent would exceed maximum depth
     */
    NavigationMenuItemResponse createMenuItem(CreateMenuItemRequest request);

    /**
     * Update an existing menu item.
     * Validates parent-child relationship and maximum depth if parent is being
     * changed.
     *
     * @param id      Menu item ID
     * @param request Update menu item request
     * @return Updated NavigationMenuItemResponse
     * @throws IllegalArgumentException if parent would exceed maximum depth
     */
    NavigationMenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request);

    /**
     * Delete a menu item.
     * Also deletes all child menu items (cascade delete).
     *
     * @param id Menu item ID
     */
    void deleteMenuItem(Long id);

    /**
     * Reorder menu items within a group.
     * Updates display_order for multiple menu items.
     *
     * @param orderedIds List of menu item IDs in the desired order
     */
    void reorderMenuItems(List<Long> orderedIds);

    /**
     * Get a single menu item by ID (for admin management).
     *
     * @param id Menu item ID
     * @return NavigationMenuItemResponse
     */
    NavigationMenuItemResponse getMenuItem(Long id);

    /**
     * Get all menu items (for admin management).
     * Returns all menu items regardless of role or permissions.
     *
     * @return List of all NavigationMenuItemResponse
     */
    List<NavigationMenuItemResponse> getAllMenuItems();
}
