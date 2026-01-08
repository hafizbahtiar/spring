package com.hafizbahtiar.spring.features.auth.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for password reset tokens.
 * Tokens are used to securely reset user passwords via email.
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_password_reset_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_password_reset_tokens_token", columnList = "token", unique = true),
        @Index(name = "idx_password_reset_tokens_expires_at", columnList = "expires_at"),
        @Index(name = "idx_password_reset_tokens_user_used", columnList = "user_id, used")
})
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {

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
     * Unique token (UUID) for password reset
     */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /**
     * Expiration timestamp for the token
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
     * Constructor for creating a new password reset token
     *
     * @param user      User requesting password reset
     * @param expiresAt Expiration time for the token
     */
    public PasswordResetToken(User user, LocalDateTime expiresAt) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PasswordResetToken that = (PasswordResetToken) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", token='" + token + '\'' +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                ", createdAt=" + createdAt +
                '}';
    }
}
