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

/**
 * Experience entity for storing work experiences.
 * Represents a user's work experience at a company or organization.
 */
@Entity
@Table(name = "portfolio_experiences", indexes = {
        @Index(name = "idx_portfolio_experiences_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_experiences_is_current", columnList = "is_current"),
        @Index(name = "idx_portfolio_experiences_start_date", columnList = "start_date"),
        @Index(name = "idx_portfolio_experiences_user_current", columnList = "user_id, is_current"),
        @Index(name = "idx_portfolio_experiences_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this experience
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Company or organization name
     */
    @Column(name = "company", nullable = false, length = 200)
    private String company;

    /**
     * Job position or title
     */
    @Column(name = "position", nullable = false, length = 200)
    private String position;

    /**
     * Job description
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Start date of employment
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of employment (nullable for current position)
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Work location address (city, country, etc.)
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Work location latitude (for map display)
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Work location longitude (for map display)
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Employment type (FULL_TIME, PART_TIME, CONTRACT, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 50)
    private EmploymentType employmentType;

    /**
     * Whether this is the current position
     */
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    /**
     * Display order for sorting experiences
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
     * Timestamp when experience was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when experience was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new experience
     */
    public Experience(User user, String company, String position, LocalDate startDate, EmploymentType employmentType) {
        this.user = user;
        this.company = company;
        this.position = position;
        this.startDate = startDate;
        this.employmentType = employmentType;
        this.isCurrent = false;
        this.displayOrder = 0;
    }

    // Business methods

    /**
     * Check if experience is current
     */
    public boolean isCurrent() {
        return Boolean.TRUE.equals(this.isCurrent);
    }

    /**
     * Set as current experience
     */
    public void setAsCurrent() {
        this.isCurrent = true;
        this.endDate = null;
    }

    /**
     * Set as past experience
     */
    public void setAsPast(LocalDate endDate) {
        this.isCurrent = false;
        this.endDate = endDate;
    }

    /**
     * Check if end date is after start date
     */
    public boolean isValidDateRange() {
        if (endDate == null) {
            return true; // Current position
        }
        return !endDate.isBefore(startDate);
    }

    /**
     * Get duration in months (approximate)
     */
    public long getDurationInMonths() {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.MONTHS.between(startDate, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Experience experience = (Experience) o;
        return id != null && id.equals(experience.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Experience{" +
                "id=" + id +
                ", company='" + company + '\'' +
                ", position='" + position + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isCurrent=" + isCurrent +
                ", employmentType=" + employmentType +
                ", createdAt=" + createdAt +
                '}';
    }
}
