package com.hafizbahtiar.spring.features.user.repository;

import com.hafizbahtiar.spring.features.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for email verification tokens.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /**
     * Find token by token string (UUID)
     *
     * @param token Token string
     * @return Optional EmailVerificationToken
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Find unverified tokens for a user
     *
     * @param userId User ID
     * @return List of unverified tokens
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.verified = false ORDER BY t.createdAt DESC")
    List<EmailVerificationToken> findByUserIdAndVerifiedFalse(@Param("userId") Long userId);

    /**
     * Check if token exists and is not verified
     *
     * @param token Token string
     * @return true if token exists and is not verified
     */
    @Query("SELECT COUNT(t) > 0 FROM EmailVerificationToken t WHERE t.token = :token AND t.verified = false")
    boolean existsByTokenAndVerifiedFalse(@Param("token") String token);

    /**
     * Find expired tokens (for cleanup)
     *
     * @param now Current timestamp
     * @return List of expired tokens
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    List<EmailVerificationToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete expired tokens (cleanup job)
     *
     * @param now Current timestamp
     * @return Number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count unverified tokens for a user
     *
     * @param userId User ID
     * @return Count of unverified tokens
     */
    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.verified = false")
    long countByUserIdAndVerifiedFalse(@Param("userId") Long userId);

    /**
     * Find token by token string and user ID (for validation)
     *
     * @param token  Token string
     * @param userId User ID
     * @return Optional EmailVerificationToken
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.token = :token AND t.user.id = :userId")
    Optional<EmailVerificationToken> findByTokenAndUserId(@Param("token") String token, @Param("userId") Long userId);
}
