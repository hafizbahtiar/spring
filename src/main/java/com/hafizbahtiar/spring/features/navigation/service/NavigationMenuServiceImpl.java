package com.hafizbahtiar.spring.features.navigation.service;

import com.hafizbahtiar.spring.common.security.Role;
import com.hafizbahtiar.spring.features.navigation.dto.CreateMenuItemRequest;
import com.hafizbahtiar.spring.features.navigation.dto.MenuItemsResponse;
import com.hafizbahtiar.spring.features.navigation.dto.NavigationMenuItemResponse;
import com.hafizbahtiar.spring.features.navigation.dto.UpdateMenuItemRequest;
import com.hafizbahtiar.spring.features.navigation.entity.NavigationMenuItem;
import com.hafizbahtiar.spring.features.navigation.repository.NavigationMenuItemRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of NavigationMenuService.
 * Handles navigation menu CRUD operations, hierarchical structures, and
 * permission-based filtering.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NavigationMenuServiceImpl implements NavigationMenuService {

    private final NavigationMenuItemRepository navigationMenuItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public MenuItemsResponse getMenuItemsForUser(Long userId) {
        log.debug("Getting menu items for user ID: {}", userId);

        // Get user and their role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        String userRole = user.getRole() != null ? user.getRole().toUpperCase() : "USER";

        // Get menu items for user's role (hierarchical structure)
        // OWNER should see all items, so fetch all active items for OWNER
        List<NavigationMenuItem> menuItems;
        if ("OWNER".equals(userRole)) {
            // OWNER can see all menu items regardless of requiredRole
            // Fetch all items as flat list and build hierarchy in service layer
            // (avoids MultipleBagFetchException from Hibernate)
            menuItems = navigationMenuItemRepository.findAllActiveItems();
        } else {
            // For other roles, fetch items matching the role and build hierarchy in service
            // layer
            menuItems = navigationMenuItemRepository
                    .findMenuItemsByRequiredRoleAndActiveTrue(userRole);
        }

        // Filter by permissions (Layer 2 - placeholder for future implementation)
        // For now, only filter by role
        List<NavigationMenuItem> filteredItems = filterMenuItemsByPermissions(menuItems, user);

        // Convert to response DTOs and build hierarchy
        List<NavigationMenuItemResponse> responseItems = buildHierarchy(filteredItems);

        // Group by group label (exclude empty groups - Dashboard has no group)
        Map<String, List<NavigationMenuItemResponse>> groupedItems = responseItems.stream()
                .filter(item -> item.getGroupLabel() != null && !item.getGroupLabel().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        NavigationMenuItemResponse::getGroupLabel,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return MenuItemsResponse.builder()
                .items(groupedItems)
                .flatItems(responseItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemsResponse getMenuItemsForRole(Role role) {
        log.debug("Getting menu items for role: {}", role);

        String roleString = role != null ? role.getValue().toUpperCase() : null;

        // Get menu items for role (flat list, hierarchy built in service layer)
        List<NavigationMenuItem> menuItems = navigationMenuItemRepository
                .findMenuItemsByRequiredRoleAndActiveTrue(roleString);

        // Convert to response DTOs and build hierarchy
        List<NavigationMenuItemResponse> responseItems = buildHierarchy(menuItems);

        // Group by group label (exclude empty groups - Dashboard has no group)
        Map<String, List<NavigationMenuItemResponse>> groupedItems = responseItems.stream()
                .filter(item -> item.getGroupLabel() != null && !item.getGroupLabel().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        NavigationMenuItemResponse::getGroupLabel,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return MenuItemsResponse.builder()
                .items(groupedItems)
                .flatItems(responseItems)
                .build();
    }

    @Override
    public NavigationMenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        log.debug("Creating menu item: {}", request.getTitle());

        NavigationMenuItem menuItem = new NavigationMenuItem();
        menuItem.setTitle(request.getTitle());
        menuItem.setUrl(request.getUrl());
        menuItem.setIconName(request.getIconName());
        menuItem.setGroupLabel(request.getGroupLabel());
        menuItem.setDisplayOrder(request.getDisplayOrder());
        menuItem.setRequiredRole(request.getRequiredRole());
        menuItem.setRequiredPermissionModule(request.getRequiredPermissionModule());
        menuItem.setRequiredPermissionPage(request.getRequiredPermissionPage());
        menuItem.setBadge(request.getBadge());
        menuItem.setActive(request.getActive() != null ? request.getActive() : true);

        // Handle parent relationship
        if (request.getParentId() != null && request.getParentId() != 0) {
            NavigationMenuItem parent = navigationMenuItemRepository.findById(request.getParentId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Parent menu item not found: " + request.getParentId()));
            menuItem.setParent(parent); // This will validate depth and set level
        } else {
            menuItem.setParent(null);
            menuItem.setLevel(0);
        }

        NavigationMenuItem saved = navigationMenuItemRepository.save(menuItem);
        log.debug("Created menu item with ID: {}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public NavigationMenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request) {
        log.debug("Updating menu item ID: {}", id);

        NavigationMenuItem menuItem = navigationMenuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));

        // Update fields if provided
        if (request.getTitle() != null) {
            menuItem.setTitle(request.getTitle());
        }
        if (request.getUrl() != null) {
            menuItem.setUrl(request.getUrl());
        }
        if (request.getIconName() != null) {
            menuItem.setIconName(request.getIconName());
        }
        if (request.getGroupLabel() != null) {
            menuItem.setGroupLabel(request.getGroupLabel());
        }
        if (request.getDisplayOrder() != null) {
            menuItem.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getRequiredRole() != null) {
            menuItem.setRequiredRole(request.getRequiredRole());
        }
        if (request.getRequiredPermissionModule() != null) {
            menuItem.setRequiredPermissionModule(request.getRequiredPermissionModule());
        }
        if (request.getRequiredPermissionPage() != null) {
            menuItem.setRequiredPermissionPage(request.getRequiredPermissionPage());
        }
        if (request.getBadge() != null) {
            menuItem.setBadge(request.getBadge());
        }
        if (request.getActive() != null) {
            menuItem.setActive(request.getActive());
        }

        // Handle parent relationship change
        if (request.getParentId() != null) {
            if (request.getParentId() == 0) {
                menuItem.setParent(null);
                menuItem.setLevel(0);
            } else {
                NavigationMenuItem parent = navigationMenuItemRepository.findById(request.getParentId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Parent menu item not found: " + request.getParentId()));
                menuItem.setParent(parent); // This will validate depth and set level
            }
        }

        NavigationMenuItem saved = navigationMenuItemRepository.save(menuItem);
        log.debug("Updated menu item ID: {}", saved.getId());

        return toResponse(saved);
    }

    @Override
    public void deleteMenuItem(Long id) {
        log.debug("Deleting menu item ID: {}", id);

        NavigationMenuItem menuItem = navigationMenuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));

        // Cascade delete will handle children automatically due to orphanRemoval = true
        navigationMenuItemRepository.delete(menuItem);
        log.debug("Deleted menu item ID: {} and its children", id);
    }

    @Override
    @Transactional
    public void reorderMenuItems(List<Long> orderedIds) {
        log.debug("Reordering {} menu items", orderedIds.size());

        if (orderedIds == null || orderedIds.isEmpty()) {
            log.warn("Received empty or null orderedIds list for reorder operation");
            return;
        }

        // Validate all IDs exist before processing
        List<NavigationMenuItem> menuItems = new ArrayList<>();
        for (Long id : orderedIds) {
            if (id == null) {
                throw new IllegalArgumentException("Menu item ID cannot be null");
            }
            NavigationMenuItem menuItem = navigationMenuItemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));
            menuItems.add(menuItem);
        }

        // Update display order for all items
        for (int i = 0; i < menuItems.size(); i++) {
            menuItems.get(i).setDisplayOrder(i + 1);
        }

        // Batch save all items
        navigationMenuItemRepository.saveAll(menuItems);
        log.debug("Reordered {} menu items", menuItems.size());
    }

    @Override
    @Transactional(readOnly = true)
    public NavigationMenuItemResponse getMenuItem(Long id) {
        log.debug("Getting menu item ID: {}", id);

        NavigationMenuItem menuItem = navigationMenuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));

        return toResponse(menuItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NavigationMenuItemResponse> getAllMenuItems() {
        log.debug("Getting all menu items");

        List<NavigationMenuItem> menuItems = navigationMenuItemRepository
                .findByActiveTrueOrderByGroupLabelAscDisplayOrderAsc();

        return menuItems.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGroupLabels() {
        log.debug("Getting distinct group labels");
        return navigationMenuItemRepository.findDistinctGroupLabels();
    }

    /**
     * Filter menu items based on user's permissions.
     * Currently only filters by role. Layer 2 permission filtering will be added
     * later.
     */
    private List<NavigationMenuItem> filterMenuItemsByPermissions(List<NavigationMenuItem> menuItems, User user) {
        String userRole = user.getRole() != null ? user.getRole().toUpperCase() : "USER";

        return menuItems.stream()
                .filter(item -> {
                    // Check role requirement
                    if (item.getRequiredRole() == null || item.getRequiredRole().isEmpty()) {
                        return true; // Available to all authenticated users
                    }

                    String requiredRole = item.getRequiredRole().toUpperCase();

                    // OWNER can see everything
                    if ("OWNER".equals(userRole)) {
                        return true;
                    }

                    // ADMIN can see ADMIN items (and items with no requiredRole)
                    if ("ADMIN".equals(userRole)) {
                        return "ADMIN".equals(requiredRole);
                    }

                    // USER can see nothing for now (only items with no requiredRole)
                    // Items without requiredRole are available to all authenticated users
                    return false;
                })
                // TODO: Add Layer 2 permission filtering when implemented
                // .filter(item -> hasPermission(user, item.getRequiredPermissionModule(),
                // item.getRequiredPermissionPage()))
                .collect(Collectors.toList());
    }

    /**
     * Build hierarchical structure from flat list of menu items.
     * Groups children under their parents recursively.
     */
    private List<NavigationMenuItemResponse> buildHierarchy(List<NavigationMenuItem> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return new ArrayList<>();
        }

        // Create a map of all items by ID for quick lookup
        Map<Long, NavigationMenuItemResponse> itemMap = menuItems.stream()
                .map(this::toResponse)
                .collect(Collectors.toMap(NavigationMenuItemResponse::getId, item -> item));

        // Build parent-child relationships
        List<NavigationMenuItemResponse> rootItems = new ArrayList<>();
        for (NavigationMenuItem item : menuItems) {
            NavigationMenuItemResponse response = itemMap.get(item.getId());
            // Get parent ID - this may trigger lazy loading, but it's necessary for
            // hierarchy building
            Long parentId = item.getParentId();

            if (parentId == null || parentId == 0) {
                // Root item
                rootItems.add(response);
            } else {
                // Child item - find parent in the map
                NavigationMenuItemResponse parent = itemMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(response);
                } else {
                    // Parent not in the filtered list (e.g., parent doesn't match role filter)
                    // Add as root item instead
                    rootItems.add(response);
                }
            }
        }

        // Sort children by display order
        sortChildrenRecursively(rootItems);

        return rootItems;
    }

    /**
     * Recursively sort children by display order.
     */
    private void sortChildrenRecursively(List<NavigationMenuItemResponse> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        items.sort(Comparator.comparing(NavigationMenuItemResponse::getDisplayOrder,
                Comparator.nullsLast(Comparator.naturalOrder())));

        for (NavigationMenuItemResponse item : items) {
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                sortChildrenRecursively(item.getChildren());
            }
        }
    }

    /**
     * Convert NavigationMenuItem entity to NavigationMenuItemResponse DTO.
     */
    private NavigationMenuItemResponse toResponse(NavigationMenuItem item) {
        return NavigationMenuItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .url(item.getUrl())
                .iconName(item.getIconName())
                .groupLabel(item.getGroupLabel())
                .displayOrder(item.getDisplayOrder())
                .parentId(item.getParentId())
                .level(item.getLevel())
                .requiredRole(item.getRequiredRole())
                .requiredPermissionModule(item.getRequiredPermissionModule())
                .requiredPermissionPage(item.getRequiredPermissionPage())
                .badge(item.getBadge())
                .active(item.getActive())
                .build();
    }
}
