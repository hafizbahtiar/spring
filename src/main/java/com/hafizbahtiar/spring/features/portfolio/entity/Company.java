package com.hafizbahtiar.spring.features.portfolio.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Company entity for storing company information.
 * Represents companies that a user has worked with or is associated with.
 */
@Entity
@Table(name = "portfolio_companies", indexes = {
        @Index(name = "idx_portfolio_companies_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_companies_name", columnList = "name"),
        @Index(name = "idx_portfolio_companies_industry", columnList = "industry"),
        @Index(name = "idx_portfolio_companies_is_verified", columnList = "is_verified"),
        @Index(name = "idx_portfolio_companies_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this company entry
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Company name
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Company logo URL (optional)
     */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /**
     * Company website URL (optional)
     */
    @Column(name = "website", length = 500)
    private String website;

    /**
     * Company description (optional)
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Industry sector (optional)
     */
    @Column(name = "industry", length = 100)
    private String industry;

    /**
     * Company location address (city, country, etc.)
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Company location latitude (for map display)
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Company location longitude (for map display)
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Year the company was founded (optional)
     */
    @Column(name = "founded_year")
    private Integer foundedYear;

    /**
     * Number of employees (optional)
     */
    @Column(name = "employee_count")
    private Integer employeeCount;

    /**
     * Whether this company is verified
     */
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    /**
     * Display order for sorting companies
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
     * Timestamp when company was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when company was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new company
     */
    public Company(User user, String name) {
        this.user = user;
        this.name = name;
        this.isVerified = false;
        this.displayOrder = 0;
    }

    // Business methods

    /**
     * Check if company is verified
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(this.isVerified);
    }

    /**
     * Verify company
     */
    public void verify() {
        this.isVerified = true;
    }

    /**
     * Unverify company
     */
    public void unverify() {
        this.isVerified = false;
    }

    /**
     * Get display name (company name)
     */
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Company company = (Company) o;
        return id != null && id.equals(company.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", industry='" + industry + '\'' +
                ", isVerified=" + isVerified +
                ", displayOrder=" + displayOrder +
                ", createdAt=" + createdAt +
                '}';
    }
}
