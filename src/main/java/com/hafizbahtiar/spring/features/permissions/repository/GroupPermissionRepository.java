package com.hafizbahtiar.spring.features.permissions.repository;

import com.hafizbahtiar.spring.features.permissions.entity.GroupPermission;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for GroupPermission entity.
 * Provides methods for querying group permissions.
 */
@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long> {

    /**
     * Find all permissions for a specific group
     *
     * @param groupId Permission group ID
     * @return List of group permissions
     */
    List<GroupPermission> findByGroupId(Long groupId);

    /**
     * Find permissions by group and permission type
     *
     * @param groupId        Permission group ID
     * @param permissionType Permission type (MODULE, PAGE, COMPONENT)
     * @return List of group permissions
     */
    List<GroupPermission> findByGroupIdAndPermissionType(Long groupId, PermissionType permissionType);

    /**
     * Find permissions by group, resource type, and resource identifier
     *
     * @param groupId            Permission group ID
     * @param resourceType       Resource type (e.g., "support", "finance")
     * @param resourceIdentifier Resource identifier (e.g., "chat", "tickets")
     * @return List of group permissions
     */
    List<GroupPermission> findByGroupIdAndResourceTypeAndResourceIdentifier(
            Long groupId, String resourceType, String resourceIdentifier);

    /**
     * Find permissions by group, permission type, resource type, resource
     * identifier, and action
     *
     * @param groupId            Permission group ID
     * @param permissionType     Permission type
     * @param resourceType       Resource type
     * @param resourceIdentifier Resource identifier
     * @param action             Permission action
     * @return Optional group permission
     */
    @Query("SELECT gp FROM GroupPermission gp WHERE gp.group.id = :groupId " +
            "AND gp.permissionType = :permissionType " +
            "AND gp.resourceType = :resourceType " +
            "AND gp.resourceIdentifier = :resourceIdentifier " +
            "AND gp.action = :action")
    List<GroupPermission> findByGroupAndPermission(
            @Param("groupId") Long groupId,
            @Param("permissionType") PermissionType permissionType,
            @Param("resourceType") String resourceType,
            @Param("resourceIdentifier") String resourceIdentifier,
            @Param("action") PermissionAction action);

    /**
     * Delete all permissions for a specific group
     *
     * @param groupId Permission group ID
     */
    void deleteByGroupId(Long groupId);
}
