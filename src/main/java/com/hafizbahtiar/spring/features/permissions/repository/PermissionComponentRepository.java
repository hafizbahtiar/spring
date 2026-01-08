package com.hafizbahtiar.spring.features.permissions.repository;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PermissionComponent entity (registry).
 * Provides methods for querying permission components.
 */
@Repository
public interface PermissionComponentRepository extends JpaRepository<PermissionComponent, Long> {

    /**
     * Find all components for a specific page
     *
     * @param pageKey Page key (e.g., "support.chat", "finance.dashboard")
     * @return List of permission components
     */
    List<PermissionComponent> findByPageKey(String pageKey);

    /**
     * Find all components for a specific page with pagination
     *
     * @param pageKey  Page key
     * @param pageable Pagination parameters
     * @return Page of permission components
     */
    Page<PermissionComponent> findByPageKey(String pageKey, Pageable pageable);

    /**
     * Find permission component by page key and component key
     *
     * @param pageKey      Page key
     * @param componentKey Component key (e.g., "edit_button", "delete_button")
     * @return Optional permission component
     */
    Optional<PermissionComponent> findByPageKeyAndComponentKey(String pageKey, String componentKey);

    /**
     * Check if permission component exists
     *
     * @param pageKey      Page key
     * @param componentKey Component key
     * @return true if exists, false otherwise
     */
    boolean existsByPageKeyAndComponentKey(String pageKey, String componentKey);

    /**
     * Search components by query (searches in componentKey, componentName, and
     * description)
     *
     * @param query    Search query
     * @param pageable Pagination parameters
     * @return Page of matching components
     */
    @Query("SELECT c FROM PermissionComponent c WHERE " +
            "LOWER(c.componentKey) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.componentName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<PermissionComponent> searchComponents(@Param("query") String query, Pageable pageable);

    /**
     * Filter components by component type
     *
     * @param componentType Component type (e.g., "BUTTON", "LINK", "MENU_ITEM")
     * @param pageable      Pagination parameters
     * @return Page of matching components
     */
    Page<PermissionComponent> findByComponentType(String componentType, Pageable pageable);
}
