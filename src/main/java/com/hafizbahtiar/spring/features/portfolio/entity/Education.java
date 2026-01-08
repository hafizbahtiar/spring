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
 * Education entity for storing educational background.
 * Represents a user's education history (degrees, certificates, etc.).
 */
@Entity
@Table(name = "portfolio_educations", indexes = {
        @Index(name = "idx_portfolio_educations_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_educations_is_current", columnList = "is_current"),
        @Index(name = "idx_portfolio_educations_start_date", columnList = "start_date"),
        @Index(name = "idx_portfolio_educations_user_current", columnList = "user_id, is_current"),
        @Index(name = "idx_portfolio_educations_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this education
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Institution name (university, school, etc.)
     */
    @Column(name = "institution", nullable = false, length = 200)
    private String institution;

    /**
     * Degree type (BACHELOR, MASTER, DOCTORATE, CERTIFICATE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "degree", nullable = false, length = 50)
    private DegreeType degree;

    /**
     * Field of study (e.g., "Computer Science", "Business Administration")
     */
    @Column(name = "field_of_study", nullable = false, length = 200)
    private String fieldOfStudy;

    /**
     * Education description (optional)
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Start date of education
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of education (nullable for ongoing education)
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Grade or GPA (optional, e.g., "3.8", "A+", "First Class")
     */
    @Column(name = "grade", length = 50)
    private String grade;

    /**
     * Institution location address (city, country, etc.)
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Institution location latitude (for map display)
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Institution location longitude (for map display)
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Whether this is current education
     */
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    /**
     * Display order for sorting educations
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
     * Timestamp when education was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when education was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new education
     */
    public Education(User user, String institution, DegreeType degree, String fieldOfStudy, LocalDate startDate) {
        this.user = user;
        this.institution = institution;
        this.degree = degree;
        this.fieldOfStudy = fieldOfStudy;
        this.startDate = startDate;
        this.isCurrent = false;
        this.displayOrder = 0;
    }

    // Business methods

    /**
     * Check if education is current
     */
    public boolean isCurrent() {
        return Boolean.TRUE.equals(this.isCurrent);
    }

    /**
     * Set as current education
     */
    public void setAsCurrent() {
        this.isCurrent = true;
        this.endDate = null;
    }

    /**
     * Set as completed education
     */
    public void setAsCompleted(LocalDate endDate) {
        this.isCurrent = false;
        this.endDate = endDate;
    }

    /**
     * Check if end date is after start date
     */
    public boolean isValidDateRange() {
        if (endDate == null) {
            return true; // Current education
        }
        return !endDate.isBefore(startDate);
    }

    /**
     * Check if degree is a higher education degree
     */
    public boolean isHigherEducation() {
        return degree != null && degree.isHigherEducation();
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
        Education education = (Education) o;
        return id != null && id.equals(education.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Education{" +
                "id=" + id +
                ", institution='" + institution + '\'' +
                ", degree=" + degree +
                ", fieldOfStudy='" + fieldOfStudy + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isCurrent=" + isCurrent +
                ", createdAt=" + createdAt +
                '}';
    }
}
