package com.hafizbahtiar.spring.features.user.repository;

import com.hafizbahtiar.spring.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find active users
    List<User> findByActiveTrue();

    // Find users by role
    List<User> findByRoleAndActiveTrue(String role);

    // Find user by email (case insensitive)
    Optional<User> findByEmailIgnoreCase(String email);

    // Find user by username (case insensitive)
    Optional<User> findByUsernameIgnoreCase(String username);

    // Find user by email or username (for login)
    @Query("SELECT u FROM User u WHERE (LOWER(u.email) = LOWER(:identifier) OR LOWER(u.username) = LOWER(:identifier)) AND u.active = true")
    Optional<User> findByEmailOrUsernameAndActive(@Param("identifier") String identifier);

    // Check if email exists
    boolean existsByEmailIgnoreCase(String email);

    // Check if username exists
    boolean existsByUsernameIgnoreCase(String username);

    // Check if email exists for another user (for updates)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    // Check if username exists for another user (for updates)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.username) = LOWER(:username) AND u.id != :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);

    // Find unverified users (for cleanup or reminders)
    List<User> findByEmailVerifiedFalseAndActiveTrue();

    // Count users by role
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.active = true")
    Long countByRole(@Param("role") String role);

    // Find recently registered users
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegistered(@Param("since") java.time.LocalDateTime since);

    // Check if OWNER role already exists (for uniqueness validation)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE UPPER(u.role) = 'OWNER' AND u.active = true")
    boolean existsOwner();

    // Find user with OWNER role (if exists)
    @Query("SELECT u FROM User u WHERE UPPER(u.role) = 'OWNER' AND u.active = true")
    Optional<User> findOwner();
}
