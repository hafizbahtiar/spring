package com.hafizbahtiar.spring.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for email verification tokens.
 * Tokens are used to securely verify user email addresses via email.
 */
@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "idx_email_verification_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_email_verification_tokens_token", columnList = "token", unique = true),
        @Index(name = "idx_email_verification_tokens_expires_at", columnList = "expires_at"),
        @Index(name = "idx_email_verification_tokens_user_verified", columnList = "user_id, verified")
})
@Getter
@Setter
@NoArgsConstructor
public class EmailVerificationToken {

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
     * Unique token (UUID) for email verification
     */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /**
     * Expiration timestamp for the token
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether this token has been used to verify the email
     */
    @Column(nullable = false)
    private Boolean verified = false;

    /**
     * Timestamp when token was created
     */
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp when token was used to verify email (null if not verified)
     */
    private LocalDateTime verifiedAt;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    /**
     * Constructor for creating a new email verification token
     *
     * @param user      User requesting email verification
     * @param expiresAt Expiration time for the token
     */
    public EmailVerificationToken(User user, LocalDateTime expiresAt) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiresAt = expiresAt;
        this.verified = false;
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
     * Check if token has been used to verify email
     *
     * @return true if token has been verified
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(this.verified);
    }

    /**
     * Check if token is valid (not expired and not verified)
     *
     * @return true if token is valid
     */
    public boolean isValid() {
        return !isExpired() && !isVerified();
    }

    /**
     * Mark token as verified
     */
    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EmailVerificationToken that = (EmailVerificationToken) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "EmailVerificationToken{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", token='" + token + '\'' +
                ", expiresAt=" + expiresAt +
                ", verified=" + verified +
                ", createdAt=" + createdAt +
                ", verifiedAt=" + verifiedAt +
                '}';
    }
}
