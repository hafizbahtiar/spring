package com.hafizbahtiar.spring.features.auth.repository;

import com.hafizbahtiar.spring.features.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for password reset tokens.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find token by token string (UUID)
     *
     * @param token Token string
     * @return Optional PasswordResetToken
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find active (unused) tokens for a user
     *
     * @param userId User ID
     * @return List of active tokens
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId AND t.used = false ORDER BY t.createdAt DESC")
    List<PasswordResetToken> findByUserIdAndUsedFalse(@Param("userId") Long userId);

    /**
     * Check if token exists and is not used
     *
     * @param token Token string
     * @return true if token exists and is not used
     */
    @Query("SELECT COUNT(t) > 0 FROM PasswordResetToken t WHERE t.token = :token AND t.used = false")
    boolean existsByTokenAndUsedFalse(@Param("token") String token);

    /**
     * Find expired tokens (for cleanup)
     *
     * @param now Current timestamp
     * @return List of expired tokens
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.expiresAt < :now")
    List<PasswordResetToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete expired tokens (cleanup job)
     *
     * @param now Current timestamp
     * @return Number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count active (unused) tokens for a user
     *
     * @param userId User ID
     * @return Count of active tokens
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user.id = :userId AND t.used = false")
    long countByUserIdAndUsedFalse(@Param("userId") Long userId);

    /**
     * Find token by token string and user ID (for validation)
     *
     * @param token  Token string
     * @param userId User ID
     * @return Optional PasswordResetToken
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.user.id = :userId")
    Optional<PasswordResetToken> findByTokenAndUserId(@Param("token") String token, @Param("userId") Long userId);
}
