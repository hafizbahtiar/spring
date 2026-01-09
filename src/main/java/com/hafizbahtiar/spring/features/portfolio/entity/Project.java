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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Project entity for storing user projects.
 * Represents a project that a user has worked on or is working on.
 */
@Entity
@Table(name = "portfolio_projects", indexes = {
        @Index(name = "idx_portfolio_projects_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_projects_status", columnList = "status"),
        @Index(name = "idx_portfolio_projects_type", columnList = "type"),
        @Index(name = "idx_portfolio_projects_platform", columnList = "platform"),
        @Index(name = "idx_portfolio_projects_is_featured", columnList = "is_featured"),
        @Index(name = "idx_portfolio_projects_start_date", columnList = "start_date"),
        @Index(name = "idx_portfolio_projects_user_status", columnList = "user_id, status"),
        @Index(name = "idx_portfolio_projects_user_type", columnList = "user_id, type"),
        @Index(name = "idx_portfolio_projects_user_platform", columnList = "user_id, platform"),
        @Index(name = "idx_portfolio_projects_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this project
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Project title
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Project description
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Technologies used (stored as JSON array)
     * Example: ["Java", "Spring Boot", "PostgreSQL", "React"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "technologies", columnDefinition = "jsonb")
    private List<String> technologies;

    /**
     * GitHub repository URL (optional)
     */
    @Column(name = "github_url", length = 500)
    private String githubUrl;

    /**
     * Live project URL (optional)
     */
    @Column(name = "live_url", length = 500)
    private String liveUrl;

    /**
     * Project image URL (optional) - kept for backward compatibility
     * @deprecated Use images array instead
     */
    @Column(name = "image_url", length = 500)
    @Deprecated
    private String imageUrl;

    /**
     * Project images (stored as JSON array of image URLs)
     * Example: ["https://example.com/image1.jpg", "https://example.com/image2.jpg"]
     * Used for displaying multiple images like Google Play Store screenshots
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", columnDefinition = "jsonb")
    private List<String> images;

    /**
     * Project platform type (WEB, ANDROID, IOS, DESKTOP, MULTI_PLATFORM, OTHER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 50)
    private PlatformType platform;

    /**
     * Project roadmap/timeline (stored as JSON array of timeline events)
     * Example: [
     *   {"date": "2024-01-01", "title": "Project Started", "description": "Initial planning"},
     *   {"date": "2024-02-01", "title": "MVP Released", "description": "First version launched"}
     * ]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roadmap", columnDefinition = "jsonb")
    private List<Map<String, Object>> roadmap;

    /**
     * Case study content (detailed project analysis, challenges, solutions, results)
     * Stored as TEXT to support long-form content
     */
    @Column(name = "case_study", columnDefinition = "TEXT")
    private String caseStudy;

    /**
     * Project start date
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Project end date (nullable for ongoing projects)
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Project location address (city, country, etc.)
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Project location latitude (for map display)
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Project location longitude (for map display)
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Project type (WORK, SIDE_HUSTLE, BUSINESS, STUDY_PERSONAL, HOBBY, OTHER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private ProjectType type = ProjectType.OTHER;

    /**
     * Project status (PLANNED, IN_PROGRESS, COMPLETED, ARCHIVED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProjectStatus status = ProjectStatus.PLANNED;

    /**
     * Whether this project is featured
     */
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    /**
     * Display order for sorting projects
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
     * Timestamp when project was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when project was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new project
     */
    public Project(User user, String title, ProjectType type, ProjectStatus status) {
        this.user = user;
        this.title = title;
        this.type = type;
        this.status = status;
        this.isFeatured = false;
        this.displayOrder = 0;
    }

    // Business methods

    /**
     * Check if project is featured
     */
    public boolean isFeatured() {
        return Boolean.TRUE.equals(this.isFeatured);
    }

    /**
     * Set project as featured
     */
    public void setFeatured() {
        this.isFeatured = true;
    }

    /**
     * Unset featured flag
     */
    public void unsetFeatured() {
        this.isFeatured = false;
    }

    /**
     * Check if project is active (planned or in progress)
     */
    public boolean isActive() {
        return status != null && status.isActive();
    }

    /**
     * Check if project is completed
     */
    public boolean isCompleted() {
        return status != null && status.isCompleted();
    }

    /**
     * Check if end date is after start date
     */
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // Ongoing project or no dates set
        }
        return !endDate.isBefore(startDate);
    }

    /**
     * Check if project has a live URL
     */
    public boolean hasLiveUrl() {
        return liveUrl != null && !liveUrl.trim().isEmpty();
    }

    /**
     * Check if project has a GitHub URL
     */
    public boolean hasGithubUrl() {
        return githubUrl != null && !githubUrl.trim().isEmpty();
    }

    /**
     * Check if project type is business-related (side hustle or main business)
     */
    public boolean isBusinessRelated() {
        return type != null && type.isBusinessRelated();
    }

    /**
     * Check if project type is professional (work or business)
     */
    public boolean isProfessional() {
        return type != null && type.isProfessional();
    }

    /**
     * Check if project type is personal (study, hobby, other)
     */
    public boolean isPersonal() {
        return type != null && type.isPersonal();
    }

    /**
     * Check if project has images
     */
    public boolean hasImages() {
        return images != null;
    }

    /**
     * Check if project has roadmap/timeline
     */
    public boolean hasRoadmap() {
        return roadmap != null;
    }

    /**
     * Check if project has case study
     */
    public boolean hasCaseStudy() {
        return caseStudy != null && !caseStudy.trim().isEmpty();
    }

    /**
     * Check if project is mobile platform (Android or iOS)
     */
    public boolean isMobilePlatform() {
        return platform != null && platform.isMobile();
    }

    /**
     * Check if project is web platform
     */
    public boolean isWebPlatform() {
        return platform != null && platform.isWeb();
    }

    /**
     * Check if project is desktop platform
     */
    public boolean isDesktopPlatform() {
        return platform != null && platform.isDesktop();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Project project = (Project) o;
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", isFeatured=" + isFeatured +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
