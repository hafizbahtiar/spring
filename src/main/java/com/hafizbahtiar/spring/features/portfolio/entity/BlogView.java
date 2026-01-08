package com.hafizbahtiar.spring.features.portfolio.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * BlogView entity for tracking blog post views.
 * Tracks views with IP address, user agent, and optional user ID for analytics.
 */
@Entity
@Table(name = "portfolio_blog_views", indexes = {
        @Index(name = "idx_blog_views_blog_id", columnList = "blog_id"),
        @Index(name = "idx_blog_views_ip_address", columnList = "ip_address"),
        @Index(name = "idx_blog_views_blog_ip", columnList = "blog_id, ip_address"),
        @Index(name = "idx_blog_views_viewed_at", columnList = "viewed_at"),
        @Index(name = "idx_blog_views_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class BlogView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Blog post that was viewed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    /**
     * IP address of the viewer
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * User agent string from the request
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * User ID if the viewer is authenticated (optional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /**
     * Timestamp when the view occurred
     */
    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;
}
