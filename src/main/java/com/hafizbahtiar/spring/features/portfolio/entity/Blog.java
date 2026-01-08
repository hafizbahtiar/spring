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

/**
 * Blog entity for storing blog posts/articles.
 * Represents a user's blog post with content, tags, and publishing status.
 */
@Entity
@Table(name = "portfolio_blogs", indexes = {
        @Index(name = "idx_portfolio_blogs_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_blogs_slug", columnList = "slug", unique = true),
        @Index(name = "idx_portfolio_blogs_published", columnList = "published"),
        @Index(name = "idx_portfolio_blogs_published_at", columnList = "published_at"),
        @Index(name = "idx_portfolio_blogs_user_published", columnList = "user_id, published"),
        @Index(name = "idx_portfolio_blogs_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this blog post
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Blog post title
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * URL-friendly slug (unique)
     * Auto-generated from title if not provided
     */
    @Column(name = "slug", nullable = false, length = 200, unique = true)
    private String slug;

    /**
     * Blog post content (HTML or Markdown)
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Short excerpt/description
     */
    @Column(name = "excerpt", length = 500)
    private String excerpt;

    /**
     * Cover image URL
     */
    @Column(name = "cover_image", length = 500)
    private String coverImage;

    /**
     * Whether this blog post is published
     */
    @Column(name = "published", nullable = false)
    private Boolean published = false;

    /**
     * Publication date (set when published becomes true)
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * Tags (stored as JSON array)
     * Example: ["technology", "programming", "spring-boot"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private Object tags;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Timestamp when blog post was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when blog post was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Business method: Publish the blog post
     */
    public void publish() {
        this.published = true;
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    /**
     * Business method: Unpublish the blog post
     */
    public void unpublish() {
        this.published = false;
    }

    /**
     * Business method: Check if blog post is published
     */
    public boolean isPublished() {
        return Boolean.TRUE.equals(this.published);
    }
}
