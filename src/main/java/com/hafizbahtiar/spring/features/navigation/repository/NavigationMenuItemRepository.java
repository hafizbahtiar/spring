package com.hafizbahtiar.spring.features.navigation.repository;

import com.hafizbahtiar.spring.features.navigation.entity.NavigationMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for NavigationMenuItem entity.
 * Provides methods for querying navigation menu items.
 */
@Repository
public interface NavigationMenuItemRepository extends JpaRepository<NavigationMenuItem, Long> {

       /**
        * Find all active menu items, ordered by group label and display order
        *
        * @return List of active menu items
        */
       List<NavigationMenuItem> findByActiveTrueOrderByGroupLabelAscDisplayOrderAsc();

       /**
        * Find menu items for a specific role, ordered by group label and display order
        *
        * @param role Required role (OWNER, ADMIN, USER) or null for all authenticated
        *             users
        * @return List of active menu items for the role
        */
       @Query("SELECT m FROM NavigationMenuItem m WHERE m.active = true " +
                     "AND (m.requiredRole = :role OR m.requiredRole IS NULL) " +
                     "ORDER BY m.groupLabel ASC, m.displayOrder ASC")
       List<NavigationMenuItem> findByRequiredRoleAndActiveTrueOrderByGroupLabelAscDisplayOrderAsc(
                     @Param("role") String role);

       /**
        * Find menu items by group label, ordered by display order
        *
        * @param groupLabel Group label (e.g., "Navigation", "Portfolio", "Admin")
        * @return List of active menu items in the group
        */
       List<NavigationMenuItem> findByGroupLabelAndActiveTrueOrderByDisplayOrderAsc(String groupLabel);

       /**
        * Find menu items that don't require a specific role (available to all
        * authenticated users)
        * and are active, ordered by group label and display order
        *
        * @return List of menu items available to all authenticated users
        */
       @Query("SELECT m FROM NavigationMenuItem m WHERE m.active = true " +
                     "AND m.requiredRole IS NULL " +
                     "ORDER BY m.groupLabel ASC, m.displayOrder ASC")
       List<NavigationMenuItem> findByActiveTrueAndRequiredRoleIsNullOrderByGroupLabelAscDisplayOrderAsc();

       /**
        * Find menu items by group label and role, ordered by display order
        *
        * @param groupLabel Group label
        * @param role       Required role or null
        * @return List of active menu items matching the criteria
        */
       @Query("SELECT m FROM NavigationMenuItem m WHERE m.active = true " +
                     "AND m.groupLabel = :groupLabel " +
                     "AND (m.requiredRole = :role OR m.requiredRole IS NULL) " +
                     "ORDER BY m.displayOrder ASC")
       List<NavigationMenuItem> findByGroupLabelAndRequiredRoleAndActiveTrueOrderByDisplayOrderAsc(
                     @Param("groupLabel") String groupLabel,
                     @Param("role") String role);

       /**
        * Find root menu items (level 0, no parent) for a specific role
        *
        * @param role Required role (OWNER, ADMIN, USER) or null for all authenticated
        *             users
        * @return List of active root menu items for the role
        */
       @Query("SELECT m FROM NavigationMenuItem m WHERE m.active = true " +
                     "AND m.level = 0 " +
                     "AND (m.parent IS NULL OR m.parent.id = 0) " +
                     "AND (m.requiredRole = :role OR m.requiredRole IS NULL) " +
                     "ORDER BY m.groupLabel ASC, m.displayOrder ASC")
       List<NavigationMenuItem> findRootItemsByRequiredRoleAndActiveTrue(
                     @Param("role") String role);

       /**
        * Find child menu items for a specific parent
        *
        * @param parentId Parent menu item ID
        * @return List of active child menu items, ordered by display order
        */
       @Query("SELECT m FROM NavigationMenuItem m WHERE m.active = true " +
                     "AND m.parent.id = :parentId " +
                     "ORDER BY m.displayOrder ASC")
       List<NavigationMenuItem> findChildrenByParentIdAndActiveTrue(@Param("parentId") Long parentId);

       /**
        * Find all menu items for a specific role (flat list)
        * Returns all active menu items matching the role criteria.
        * Hierarchy is built in the service layer to avoid MultipleBagFetchException.
        *
        * @param role Required role (OWNER, ADMIN, USER) or null for all authenticated
        *             users
        * @return List of active menu items for the role
        */
       @Query("SELECT m FROM NavigationMenuItem m " +
                     "WHERE m.active = true " +
                     "AND (m.requiredRole = :role OR m.requiredRole IS NULL) " +
                     "ORDER BY m.groupLabel ASC, m.displayOrder ASC")
       List<NavigationMenuItem> findMenuItemsByRequiredRoleAndActiveTrue(@Param("role") String role);

       /**
        * Find menu items by level
        *
        * @param level Menu item level (0, 1, or 2)
        * @return List of active menu items at the specified level
        */
       List<NavigationMenuItem> findByLevelAndActiveTrueOrderByGroupLabelAscDisplayOrderAsc(Integer level);

       /**
        * Find all active root menu items (for OWNER role - shows all items)
        * Returns root items only. Children should be loaded separately to avoid
        * MultipleBagFetchException.
        *
        * @return List of active root menu items
        */
       @Query("SELECT DISTINCT m FROM NavigationMenuItem m " +
                     "WHERE m.active = true " +
                     "AND m.level = 0 " +
                     "AND (m.parent IS NULL OR m.parent.id = 0) " +
                     "ORDER BY m.groupLabel ASC, m.displayOrder ASC")
       List<NavigationMenuItem> findAllRootItemsByActiveTrue();

       /**
        * Find all active menu items (flat list, for OWNER role)
        * Used to build hierarchy manually in service layer
        *
        * @return List of all active menu items
        */
       @Query("SELECT m FROM NavigationMenuItem m " +
                     "WHERE m.active = true " +
                     "ORDER BY m.groupLabel ASC, m.displayOrder ASC")
       List<NavigationMenuItem> findAllActiveItems();
}
