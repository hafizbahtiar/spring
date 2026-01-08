package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.entity.BlogView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for blog view tracking.
 * Handles view tracking, statistics, and view history for blog posts.
 */
public interface BlogViewService {

    /**
     * Track a view for a blog post.
     * Deduplicates views by IP address within 24 hours (same IP can only count as 1
     * view per 24 hours per blog post).
     *
     * @param blogId    Blog post ID
     * @param ipAddress IP address of the viewer
     * @param userAgent User agent string from the request
     * @param userId    User ID if the viewer is authenticated (optional, can be
     *                  null)
     * @return true if view was tracked, false if it was a duplicate (within 24
     *         hours)
     */
    boolean trackView(Long blogId, String ipAddress, String userAgent, Long userId);

    /**
     * Get total view count for a blog post.
     *
     * @param blogId Blog post ID
     * @return Total view count
     */
    long getViewCount(Long blogId);

    /**
     * Get unique IP address view count for a blog post.
     *
     * @param blogId Blog post ID
     * @return Unique IP view count
     */
    long getUniqueViewCount(Long blogId);

    /**
     * Get recent views for a blog post.
     *
     * @param blogId   Blog post ID
     * @param pageable Pagination parameters
     * @return Page of BlogView entities
     */
    Page<BlogView> getRecentViews(Long blogId, Pageable pageable);
}
