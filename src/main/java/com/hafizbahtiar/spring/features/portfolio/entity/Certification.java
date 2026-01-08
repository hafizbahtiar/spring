package com.hafizbahtiar.spring.features.portfolio.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Certification entity for storing professional certifications and credentials.
 * Represents certifications like AWS, Google Cloud, Microsoft Azure, etc.
 */
@Entity
@Table(name = "portfolio_certifications", indexes = {
        @Index(name = "idx_portfolio_certifications_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_certifications_issuer", columnList = "issuer"),
        @Index(name = "idx_portfolio_certifications_issue_date", columnList = "issue_date"),
        @Index(name = "idx_portfolio_certifications_expiry_date", columnList = "expiry_date"),
        @Index(name = "idx_portfolio_certifications_is_expired", columnList = "is_expired"),
        @Index(name = "idx_portfolio_certifications_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this certification
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Certification name (e.g., "AWS Certified Solutions Architect")
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Issuing organization (e.g., "AWS", "Google Cloud", "Microsoft")
     */
    @Column(name = "issuer", nullable = false, length = 100)
    private String issuer;

    /**
     * Credential ID or certificate number (optional)
     */
    @Column(name = "credential_id", length = 100)
    private String credentialId;

    /**
     * URL to verify or view the credential (optional)
     */
    @Column(name = "credential_url", length = 500)
    private String credentialUrl;

    /**
     * Issue date of the certification
     */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /**
     * Expiry date of the certification (nullable for non-expiring certifications)
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * Whether this certification is expired
     */
    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired = false;

    /**
     * Whether this certification is verified
     */
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    /**
     * Certification image or badge URL (optional)
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Certification description (optional)
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Display order for sorting certifications
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Timestamp when certification was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when certification was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new certification
     */
    public Certification(User user, String name, String issuer, LocalDate issueDate) {
        this.user = user;
        this.name = name;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.isExpired = false;
        this.isVerified = false;
        this.displayOrder = 0;
    }

    // Business methods

    /**
     * Check if certification is expired
     * Updates isExpired flag if expiry date has passed
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            // No expiry date means it doesn't expire
            this.isExpired = false;
            return false;
        }
        boolean expired = LocalDate.now().isAfter(expiryDate);
        this.isExpired = expired;
        return expired;
    }

    /**
     * Calculate days until expiry
     * Returns null if no expiry date or already expired
     */
    public Long daysUntilExpiry() {
        if (expiryDate == null) {
            return null; // No expiry date
        }
        if (isExpired()) {
            return null; // Already expired
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Check if certification is expiring soon (within specified days)
     * 
     * @param days Number of days to check ahead (default: 90)
     * @return true if expiring within the specified days
     */
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null || isExpired()) {
            return false;
        }
        Long daysUntil = daysUntilExpiry();
        return daysUntil != null && daysUntil <= days;
    }

    /**
     * Check if certification is expiring soon (within 90 days)
     */
    public boolean isExpiringSoon() {
        return isExpiringSoon(90);
    }

    /**
     * Verify certification
     */
    public void verify() {
        this.isVerified = true;
    }

    /**
     * Unverify certification
     */
    public void unverify() {
        this.isVerified = false;
    }

    /**
     * Check if certification is verified
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(this.isVerified);
    }

    /**
     * Get display name (certification name)
     */
    public String getDisplayName() {
        return this.name;
    }

    /**
     * Check if certification has an expiry date
     */
    public boolean hasExpiryDate() {
        return expiryDate != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Certification that = (Certification) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Certification{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", issuer='" + issuer + '\'' +
                ", issueDate=" + issueDate +
                ", expiryDate=" + expiryDate +
                ", isExpired=" + isExpired +
                ", isVerified=" + isVerified +
                ", displayOrder=" + displayOrder +
                ", createdAt=" + createdAt +
                '}';
    }
}
