package com.hafizbahtiar.spring.features.permissions.repository;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PermissionModule entity (registry).
 * Provides methods for querying permission modules.
 */
@Repository
public interface PermissionModuleRepository extends JpaRepository<PermissionModule, Long> {

    /**
     * Find permission module by module key
     *
     * @param moduleKey Module key (e.g., "support", "finance", "portfolio")
     * @return Optional permission module
     */
    Optional<PermissionModule> findByModuleKey(String moduleKey);

    /**
     * Check if permission module with key exists
     *
     * @param moduleKey Module key
     * @return true if exists, false otherwise
     */
    boolean existsByModuleKey(String moduleKey);

    /**
     * Search modules by query (searches in moduleKey, moduleName, and description)
     *
     * @param query    Search query
     * @param pageable Pagination parameters
     * @return Page of matching modules
     */
    @Query("SELECT m FROM PermissionModule m WHERE " +
            "LOWER(m.moduleKey) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.moduleName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<PermissionModule> searchModules(@Param("query") String query, Pageable pageable);

    /**
     * Filter modules by available roles
     *
     * @param availableToRoles Comma-separated roles (e.g., "OWNER,ADMIN")
     * @param pageable         Pagination parameters
     * @return Page of matching modules
     */
    @Query("SELECT m FROM PermissionModule m WHERE " +
            "m.availableToRoles LIKE CONCAT('%', :role, '%') OR " +
            "m.availableToRoles IS NULL OR " +
            "m.availableToRoles = ''")
    Page<PermissionModule> findByAvailableToRolesContaining(@Param("role") String role, Pageable pageable);
}
