package com.hafizbahtiar.spring.features.permissions.repository;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PermissionPage entity (registry).
 * Provides methods for querying permission pages.
 */
@Repository
public interface PermissionPageRepository extends JpaRepository<PermissionPage, Long> {

    /**
     * Find all pages for a specific module
     *
     * @param moduleKey Module key (e.g., "support", "finance")
     * @return List of permission pages
     */
    List<PermissionPage> findByModuleKey(String moduleKey);

    /**
     * Find all pages for a specific module with pagination
     *
     * @param moduleKey Module key
     * @param pageable  Pagination parameters
     * @return Page of permission pages
     */
    Page<PermissionPage> findByModuleKey(String moduleKey, Pageable pageable);

    /**
     * Find permission page by module key and page key
     *
     * @param moduleKey Module key
     * @param pageKey   Page key (e.g., "chat", "tickets")
     * @return Optional permission page
     */
    Optional<PermissionPage> findByModuleKeyAndPageKey(String moduleKey, String pageKey);

    /**
     * Check if permission page exists
     *
     * @param moduleKey Module key
     * @param pageKey   Page key
     * @return true if exists, false otherwise
     */
    boolean existsByModuleKeyAndPageKey(String moduleKey, String pageKey);

    /**
     * Search pages by query (searches in pageKey, pageName, routePath, and
     * description)
     *
     * @param query    Search query
     * @param pageable Pagination parameters
     * @return Page of matching pages
     */
    @Query("SELECT p FROM PermissionPage p WHERE " +
            "LOWER(p.pageKey) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.pageName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.routePath) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<PermissionPage> searchPages(@Param("query") String query, Pageable pageable);
}
