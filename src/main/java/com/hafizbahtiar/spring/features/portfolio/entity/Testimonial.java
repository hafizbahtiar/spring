package com.hafizbahtiar.spring.features.portfolio.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Testimonial entity for storing testimonials/reviews.
 * Represents testimonials from clients, colleagues, or users about a person's
 * work or projects.
 */
@Entity
@Table(name = "portfolio_testimonials", indexes = {
        @Index(name = "idx_portfolio_testimonials_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_testimonials_is_featured", columnList = "is_featured"),
        @Index(name = "idx_portfolio_testimonials_is_approved", columnList = "is_approved"),
        @Index(name = "idx_portfolio_testimonials_rating", columnList = "rating"),
        @Index(name = "idx_portfolio_testimonials_display_order", columnList = "display_order"),
        @Index(name = "idx_portfolio_testimonials_project_id", columnList = "project_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this testimonial (the person being reviewed)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Optional project this testimonial is related to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * Author name (person giving the testimonial)
     */
    @Column(name = "author_name", nullable = false, length = 200)
    private String authorName;

    /**
     * Author title/position (e.g., "CEO", "Senior Developer")
     */
    @Column(name = "author_title", length = 200)
    private String authorTitle;

    /**
     * Author company
     */
    @Column(name = "author_company", length = 200)
    private String authorCompany;

    /**
     * Author image URL (optional)
     */
    @Column(name = "author_image_url", length = 500)
    private String authorImageUrl;

    /**
     * Testimonial content/review text
     */
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    /**
     * Rating (1-5 stars)
     */
    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * Whether this testimonial is featured
     */
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    /**
     * Whether this testimonial is approved (for moderation)
     */
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

    /**
     * Display order for sorting testimonials
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
     * Timestamp when testimonial was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when testimonial was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new testimonial
     */
    public Testimonial(User user, String authorName, String content, Integer rating) {
        this.user = user;
        this.authorName = authorName;
        this.content = content;
        this.rating = rating;
        this.isFeatured = false;
        this.isApproved = false;
        this.displayOrder = 0;
    }

    // Business methods

    /**
     * Check if testimonial is approved
     */
    public boolean isApproved() {
        return Boolean.TRUE.equals(this.isApproved);
    }

    /**
     * Approve testimonial
     */
    public void approve() {
        this.isApproved = true;
    }

    /**
     * Unapprove testimonial
     */
    public void unapprove() {
        this.isApproved = false;
    }

    /**
     * Check if testimonial is featured
     */
    public boolean isFeatured() {
        return Boolean.TRUE.equals(this.isFeatured);
    }

    /**
     * Set testimonial as featured
     */
    public void setFeatured(boolean featured) {
        this.isFeatured = featured;
    }

    /**
     * Get rating as stars string (e.g., "★★★★★" for 5 stars)
     */
    public String getRatingStars() {
        if (rating == null) {
            return "";
        }
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    /**
     * Check if rating is high (4 or 5 stars)
     */
    public boolean isHighRating() {
        return rating != null && rating >= 4;
    }

    /**
     * Get display name (author name)
     */
    public String getDisplayName() {
        return this.authorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Testimonial that = (Testimonial) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Testimonial{" +
                "id=" + id +
                ", authorName='" + authorName + '\'' +
                ", rating=" + rating +
                ", isFeatured=" + isFeatured +
                ", isApproved=" + isApproved +
                ", displayOrder=" + displayOrder +
                ", createdAt=" + createdAt +
                '}';
    }
}
