package com.hafizbahtiar.spring.features.navigation.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.navigation.dto.CreateMenuItemRequest;
import com.hafizbahtiar.spring.features.navigation.dto.MenuItemsResponse;
import com.hafizbahtiar.spring.features.navigation.dto.NavigationMenuItemResponse;
import com.hafizbahtiar.spring.features.navigation.dto.ReorderMenuItemsRequest;
import com.hafizbahtiar.spring.features.navigation.dto.UpdateMenuItemRequest;
import com.hafizbahtiar.spring.features.navigation.service.NavigationMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for navigation menu management endpoints.
 * Handles menu item CRUD operations and menu retrieval for users.
 */
@RestController
@RequestMapping("/api/v1/navigation")
@RequiredArgsConstructor
@Slf4j
public class NavigationMenuController {

    private final NavigationMenuService navigationMenuService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Get current user's menu items
     * GET /api/v1/navigation/menu
     * Requires: Authenticated user
     */
    @GetMapping("/menu")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MenuItemsResponse>> getMenuItems() {
        Long userId = getCurrentUserId();
        log.debug("Fetching menu items for user ID: {}", userId);
        MenuItemsResponse response = navigationMenuService.getMenuItemsForUser(userId);
        return ResponseUtils.ok(response);
    }

    /**
     * Get all menu items for management (admin view)
     * GET /api/v1/navigation/menu/admin
     * Requires: OWNER (always) or ADMIN (with permission)
     * TODO: When Layer 2 permissions are implemented, replace hasRole('ADMIN') with
     * permission check
     */
    @GetMapping("/menu/admin")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemsResponse>> getAllMenuItemsForAdmin() {
        log.debug("Fetching all menu items for admin management");
        // Get menu items for OWNER role (shows all items regardless of role
        // requirement)
        MenuItemsResponse response = navigationMenuService.getMenuItemsForRole(
                com.hafizbahtiar.spring.common.security.Role.OWNER);
        return ResponseUtils.ok(response);
    }

    /**
     * Get all menu items as flat list (for admin management)
     * GET /api/v1/navigation/menu/items
     * Requires: OWNER (always) or ADMIN (with permission)
     * TODO: When Layer 2 permissions are implemented, replace hasRole('ADMIN') with
     * permission check
     */
    @GetMapping("/menu/items")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<NavigationMenuItemResponse>>> getAllMenuItemsList() {
        log.debug("Fetching all menu items as flat list for admin management");
        List<NavigationMenuItemResponse> response = navigationMenuService.getAllMenuItems();
        return ResponseUtils.ok(response);
    }

    /**
     * Get distinct group labels (for admin management)
     * GET /api/v1/navigation/menu/groups
     * Requires: OWNER (always) or ADMIN (with permission)
     * TODO: When Layer 2 permissions are implemented, replace hasRole('ADMIN') with
     * permission check
     */
    @GetMapping("/menu/groups")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>> getGroupLabels() {
        log.debug("Fetching distinct group labels for admin management");
        List<String> response = navigationMenuService.getGroupLabels();
        return ResponseUtils.ok(response);
    }

    /**
     * Get single menu item by ID
     * GET /api/v1/navigation/menu/items/{id}
     * Requires: OWNER (always) or ADMIN (with permission)
     * TODO: When Layer 2 permissions are implemented, replace hasRole('ADMIN') with
     * permission check
     */
    @GetMapping("/menu/items/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NavigationMenuItemResponse>> getMenuItem(@PathVariable Long id) {
        log.debug("Fetching menu item ID: {}", id);
        NavigationMenuItemResponse response = navigationMenuService.getMenuItem(id);
        return ResponseUtils.ok(response);
    }

    /**
     * Create a new menu item
     * POST /api/v1/navigation/menu/items
     * Requires: OWNER (always) or ADMIN (with permission)
     * TODO: When Layer 2 permissions are implemented, replace hasRole('ADMIN') with
     * permission check
     */
    @PostMapping("/menu/items")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NavigationMenuItemResponse>> createMenuItem(
            @Valid @RequestBody CreateMenuItemRequest request) {
        log.info("Menu item creation request received: {}", request.getTitle());
        NavigationMenuItemResponse response = navigationMenuService.createMenuItem(request);
        return ResponseUtils.created(response, "Menu item created successfully");
    }

    /**
     * Update an existing menu item
     * PUT /api/v1/navigation/menu/items/{id}
     * Requires: OWNER (always) or ADMIN (with permission)
     * TODO: When Layer 2 permissions are implemented, replace hasRole('ADMIN') with
     * permission check
     */
    @PutMapping("/menu/items/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NavigationMenuItemResponse>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        log.info("Menu item update request received for ID: {}", id);
        NavigationMenuItemResponse response = navigationMenuService.updateMenuItem(id, request);
        return ResponseUtils.ok(response, "Menu item updated successfully");
    }

    /**
     * Delete a menu item
     * DELETE /api/v1/navigation/menu/items/{id}
     * Requires: OWNER (always) or ADMIN (with permission)
     * TODO: When Layer 2 permissions are implemented, replace hasRole('ADMIN') with
     * permission check
     */
    @DeleteMapping("/menu/items/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        log.info("Menu item deletion request received for ID: {}", id);
        navigationMenuService.deleteMenuItem(id);
        return ResponseUtils.noContent();
    }

    /**
     * Reorder menu items
     * PUT /api/v1/navigation/menu/items/reorder
     * Requires: OWNER (always) or ADMIN (with permission)
     * TODO: When Layer 2 permissions are implemented, replace hasRole('ADMIN') with
     * permission check
     */
    @PutMapping("/menu/items/reorder")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reorderMenuItems(
            @Valid @RequestBody ReorderMenuItemsRequest request) {
        log.info("Menu items reorder request received for {} items",
                request.getOrderedIds() != null ? request.getOrderedIds().size() : 0);

        try {
            navigationMenuService.reorderMenuItems(request.getOrderedIds());
            return ResponseUtils.ok(null, "Menu items reordered successfully");
        } catch (IllegalArgumentException e) {
            log.error("Invalid reorder request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error reordering menu items", e);
            throw new RuntimeException("Failed to reorder menu items: " + e.getMessage(), e);
        }
    }
}
