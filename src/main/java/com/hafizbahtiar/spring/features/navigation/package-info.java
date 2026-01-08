/**
 * Navigation Menu Feature Module
 * 
 * <p>
 * This package contains the navigation menu system that provides dynamic,
 * database-driven navigation menu items for the frontend application.
 * 
 * <p>
 * The navigation menu system allows:
 * <ul>
 * <li>Dynamic menu item management (create, update, delete, reorder)</li>
 * <li>Hierarchical menu structure with maximum 3 levels (root level 0, then
 * level 1, then level 2)</li>
 * <li>Role-based menu item visibility (OWNER, ADMIN, USER)</li>
 * <li>Permission-based menu item filtering (when Layer 2 permission system is
 * implemented)</li>
 * <li>Grouped menu items (Navigation, Portfolio, Admin, etc.)</li>
 * </ul>
 * 
 * <p>
 * Key Components:
 * <ul>
 * <li>{@link com.hafizbahtiar.spring.features.navigation.entity.NavigationMenuItem}
 * - Entity for menu items</li>
 * <li>{@link com.hafizbahtiar.spring.features.navigation.repository.NavigationMenuItemRepository}
 * - Repository for menu items</li>
 * </ul>
 * 
 * @author Hafiz Bahtiar
 * @since 1.0
 */
package com.hafizbahtiar.spring.features.navigation;
