package com.hafizbahtiar.spring.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for account deletion tokens.
 * Tokens are used to securely confirm account deletion via email.
 */
@Entity
@Table(name = "account_deletion_tokens", indexes = {
        @Index(name = "idx_account_deletion_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_account_deletion_tokens_token", columnList = "token", unique = true),
        @Index(name = "idx_account_deletion_tokens_expires_at", columnList = "expires_at"),
        @Index(name = "idx_account_deletion_tokens_user_used", columnList = "user_id, used")
})
@Getter
@Setter
@NoArgsConstructor
public class AccountDeletionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User associated with this token
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique token (UUID) for account deletion
     */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /**
     * Expiration timestamp for the token (typically 7 days)
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether this token has been used
     */
    @Column(nullable = false)
    private Boolean used = false;

    /**
     * Timestamp when token was created
     */
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp when token was used (null if not used)
     */
    private LocalDateTime usedAt;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    /**
     * Constructor for creating a new account deletion token
     *
     * @param user      User requesting account deletion
     * @param expiresAt Expiration time for the token
     */
    public AccountDeletionToken(User user, LocalDateTime expiresAt) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiresAt = expiresAt;
        this.used = false;
    }

    /**
     * Check if token is expired
     *
     * @return true if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token has been used
     *
     * @return true if token has been used
     */
    public boolean isUsed() {
        return Boolean.TRUE.equals(this.used);
    }

    /**
     * Check if token is valid (not expired and not used)
     *
     * @return true if token is valid
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * Mark token as used
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}
