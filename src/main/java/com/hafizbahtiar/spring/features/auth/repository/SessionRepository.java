package com.hafizbahtiar.spring.features.auth.repository;

import com.hafizbahtiar.spring.features.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Session entity.
 * Provides CRUD operations for session management.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    /**
     * Find all active sessions for a user
     *
     * @param userId User ID
     * @return List of active sessions
     */
    List<Session> findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(Long userId);

    /**
     * Find session by session ID
     *
     * @param sessionId Session ID (UUID)
     * @return Optional Session
     */
    Optional<Session> findBySessionId(String sessionId);

    /**
     * Find active session by session ID
     *
     * @param sessionId Session ID (UUID)
     * @return Optional Session
     */
    Optional<Session> findBySessionIdAndIsActiveTrue(String sessionId);

    /**
     * Find all sessions (active and inactive) for a user
     *
     * @param userId User ID
     * @return List of all sessions
     */
    List<Session> findByUserIdOrderByLastActivityAtDesc(Long userId);

    /**
     * Revoke all sessions for a user except the current one
     *
     * @param userId           User ID
     * @param currentSessionId Current session ID to keep active
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.lastActivityAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.sessionId != :currentSessionId AND s.isActive = true")
    void revokeAllSessionsExceptCurrent(@Param("userId") Long userId,
            @Param("currentSessionId") String currentSessionId);

    /**
     * Revoke a specific session
     *
     * @param sessionId Session ID to revoke
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false, s.lastActivityAt = CURRENT_TIMESTAMP WHERE s.sessionId = :sessionId")
    void revokeSession(@Param("sessionId") String sessionId);

    /**
     * Delete all inactive sessions older than specified days (for cleanup)
     *
     * @param days Number of days
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.isActive = false AND s.lastActivityAt < :cutoffDate")
    void deleteInactiveSessionsOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Count active sessions for a user
     *
     * @param userId User ID
     * @return Number of active sessions
     */
    long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find active session by session ID with valid refresh token expiration
     * Used for refresh token validation in two-token authentication strategy
     *
     * @param sessionId Session ID (UUID, used as refresh token)
     * @param now       Current timestamp to check expiration
     * @return Optional Session
     */
    Optional<Session> findBySessionIdAndIsActiveTrueAndRefreshTokenExpiresAtAfter(String sessionId, LocalDateTime now);

    /**
     * Find all sessions with expired refresh tokens
     * Used for cleanup of expired refresh tokens
     *
     * @param now Current timestamp
     * @return List of sessions with expired refresh tokens
     */
    List<Session> findByRefreshTokenExpiresAtBefore(LocalDateTime now);
}
