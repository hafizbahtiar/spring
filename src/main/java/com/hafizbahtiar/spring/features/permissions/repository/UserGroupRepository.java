package com.hafizbahtiar.spring.features.permissions.repository;

import com.hafizbahtiar.spring.features.permissions.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserGroup entity.
 * Provides methods for querying user-group assignments.
 */
@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    /**
     * Find all groups for a specific user
     *
     * @param userId User ID
     * @return List of user-group assignments
     */
    List<UserGroup> findByUserId(Long userId);

    /**
     * Find all users in a specific group
     *
     * @param groupId Permission group ID
     * @return List of user-group assignments
     */
    List<UserGroup> findByGroupId(Long groupId);

    /**
     * Find specific user-group assignment
     *
     * @param userId  User ID
     * @param groupId Permission group ID
     * @return Optional user-group assignment
     */
    Optional<UserGroup> findByUserIdAndGroupId(Long userId, Long groupId);

    /**
     * Check if user is assigned to a group
     *
     * @param userId  User ID
     * @param groupId Permission group ID
     * @return true if assigned, false otherwise
     */
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    /**
     * Find all active groups for a user (groups that are active)
     *
     * @param userId User ID
     * @return List of user-group assignments where group is active
     */
    @Query("SELECT ug FROM UserGroup ug WHERE ug.user.id = :userId AND ug.group.active = true")
    List<UserGroup> findActiveGroupsByUserId(@Param("userId") Long userId);

    /**
     * Delete all assignments for a specific user
     *
     * @param userId User ID
     */
    void deleteByUserId(Long userId);

    /**
     * Delete all assignments for a specific group
     *
     * @param groupId Permission group ID
     */
    void deleteByGroupId(Long groupId);
}
