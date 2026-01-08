package com.hafizbahtiar.spring.features.user.repository;

import com.hafizbahtiar.spring.features.user.entity.AccountDeletionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for AccountDeletionToken entity.
 * Provides CRUD operations for account deletion token management.
 */
@Repository
public interface AccountDeletionTokenRepository extends JpaRepository<AccountDeletionToken, Long> {

    /**
     * Find token by token string
     *
     * @param token Token string
     * @return Optional AccountDeletionToken
     */
    Optional<AccountDeletionToken> findByToken(String token);

    /**
     * Find active (unused and not expired) token by token string
     *
     * @param token Token string
     * @return Optional AccountDeletionToken
     */
    @Query("SELECT t FROM AccountDeletionToken t WHERE t.token = :token AND t.used = false AND t.expiresAt > :now")
    Optional<AccountDeletionToken> findActiveTokenByToken(@Param("token") String token,
            @Param("now") LocalDateTime now);

    /**
     * Find active token by user ID
     *
     * @param userId User ID
     * @return Optional AccountDeletionToken
     */
    @Query("SELECT t FROM AccountDeletionToken t WHERE t.user.id = :userId AND t.used = false AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    Optional<AccountDeletionToken> findActiveTokenByUserId(@Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    /**
     * Delete all expired tokens
     */
    @Modifying
    @Query("DELETE FROM AccountDeletionToken t WHERE t.expiresAt < :now OR (t.used = true AND t.usedAt < :cutoffDate)")
    void deleteExpiredTokens(@Param("now") LocalDateTime now, @Param("cutoffDate") LocalDateTime cutoffDate);
}

