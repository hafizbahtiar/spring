package com.hafizbahtiar.spring.features.portfolio.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Portfolio Profile entity for storing user's portfolio-specific profile information.
 * One-to-one relationship with User entity (one profile per user).
 */
@Entity
@Table(name = "portfolio_profiles", indexes = {
        @Index(name = "idx_portfolio_profiles_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_portfolio_profiles_uuid", columnList = "uuid", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class PortfolioProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public UUID for portfolio profile identification (exposed in APIs)
     * Generated automatically on creation via @PrePersist
     */
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    /**
     * User who owns this profile (one-to-one relationship)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * User's bio or about section
     */
    @Column(name = "bio", length = 2000)
    private String bio;

    /**
     * User's location (city, country, etc.)
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * Availability status (e.g., "Available", "Busy", "Not Available")
     */
    @Column(name = "availability", length = 50)
    private String availability;

    /**
     * URL to user's avatar/profile picture
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * URL to user's resume/CV
     */
    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    /**
     * Social media links (LinkedIn, GitHub, Twitter, etc.)
     * Stored as JSONB in PostgreSQL
     * Key: Platform name (e.g., "linkedin", "github", "twitter")
     * Value: URL to the profile
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    private Object socialLinks;

    /**
     * User preferences (theme, language, etc.)
     * Stored as JSONB in PostgreSQL
     * Key: Preference name (e.g., "theme", "language")
     * Value: Preference value (can be String, Boolean, Number, etc.)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "jsonb")
    private Object preferences;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Timestamp when profile was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when profile was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Generate UUID before persisting if not already set
     */
    @PrePersist
    protected void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }
}

