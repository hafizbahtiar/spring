package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.BlogView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BlogView entity.
 * Provides methods for querying blog view statistics and history.
 */
@Repository
public interface BlogViewRepository extends JpaRepository<BlogView, Long> {

    /**
     * Count total views for a blog post
     *
     * @param blogId Blog post ID
     * @return Total view count
     */
    long countByBlogId(Long blogId);

    /**
     * Count distinct IP addresses that viewed a blog post
     *
     * @param blogId Blog post ID
     * @return Unique IP count
     */
    @Query("SELECT COUNT(DISTINCT bv.ipAddress) FROM BlogView bv WHERE bv.blog.id = :blogId")
    long countDistinctIpAddressByBlogId(Long blogId);

    /**
     * Check if a specific IP address has viewed a blog post
     *
     * @param blogId    Blog post ID
     * @param ipAddress IP address to check
     * @return true if IP has viewed the blog post
     */
    boolean existsByBlogIdAndIpAddress(Long blogId, String ipAddress);

    /**
     * Find recent views for a blog post, ordered by viewedAt descending
     *
     * @param blogId   Blog post ID
     * @param pageable Pagination parameters
     * @return List of recent views
     */
    List<BlogView> findByBlogIdOrderByViewedAtDesc(Long blogId, Pageable pageable);

    /**
     * Check if a specific IP address has viewed a blog post within a time window
     * Used for deduplication (e.g., same IP can only count as 1 view per 24 hours)
     *
     * @param blogId    Blog post ID
     * @param ipAddress IP address to check
     * @param since     Start of time window
     * @return true if IP has viewed the blog post within the time window
     */
    boolean existsByBlogIdAndIpAddressAndViewedAtAfter(Long blogId, String ipAddress, LocalDateTime since);
}
