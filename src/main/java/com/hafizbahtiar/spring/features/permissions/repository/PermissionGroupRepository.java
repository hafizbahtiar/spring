package com.hafizbahtiar.spring.features.permissions.repository;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PermissionGroup entity.
 * Provides methods for querying permission groups.
 */
@Repository
public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {

    /**
     * Find all active permission groups
     *
     * @return List of active permission groups
     */
    List<PermissionGroup> findByActiveTrue();

    /**
     * Find permission group by name
     *
     * @param name Group name
     * @return Optional permission group
     */
    Optional<PermissionGroup> findByName(String name);

    /**
     * Check if permission group with name exists
     *
     * @param name Group name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Find all permission groups created by a specific user
     *
     * @param createdById User ID who created the groups
     * @return List of permission groups
     */
    List<PermissionGroup> findByCreatedById(Long createdById);
}
