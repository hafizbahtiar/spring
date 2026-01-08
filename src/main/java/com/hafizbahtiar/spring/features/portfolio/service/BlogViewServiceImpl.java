package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.entity.Blog;
import com.hafizbahtiar.spring.features.portfolio.entity.BlogView;
import com.hafizbahtiar.spring.features.portfolio.exception.BlogNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.repository.BlogRepository;
import com.hafizbahtiar.spring.features.portfolio.repository.BlogViewRepository;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of BlogViewService.
 * Handles blog view tracking with deduplication, statistics, and view history.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BlogViewServiceImpl implements BlogViewService {

    private final BlogViewRepository blogViewRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    // Deduplication window: 24 hours
    private static final int DEDUPLICATION_HOURS = 24;

    @Override
    @Transactional
    public boolean trackView(Long blogId, String ipAddress, String userAgent, Long userId) {
        log.debug("Tracking view for blog ID: {}, IP: {}, userId: {}", blogId, ipAddress, userId);

        // Validate blog exists
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> BlogNotFoundException.byId(blogId));

        // Only track views for published blogs
        if (!blog.isPublished()) {
            log.debug("Skipping view tracking for unpublished blog ID: {}", blogId);
            return false;
        }

        // Check for duplicate view within 24 hours
        LocalDateTime since = LocalDateTime.now().minusHours(DEDUPLICATION_HOURS);
        boolean isDuplicate = blogViewRepository.existsByBlogIdAndIpAddressAndViewedAtAfter(
                blogId, ipAddress, since);

        if (isDuplicate) {
            log.debug("Duplicate view detected for blog ID: {}, IP: {} (within 24 hours)", blogId, ipAddress);
            return false;
        }

        // Create and save view
        BlogView blogView = new BlogView();
        blogView.setBlog(blog);
        blogView.setIpAddress(ipAddress);
        blogView.setUserAgent(userAgent);

        // Set user if provided and exists
        if (userId != null) {
            userRepository.findById(userId).ifPresent(blogView::setUser);
        }

        blogViewRepository.save(blogView);
        log.info("View tracked for blog ID: {}, IP: {}", blogId, ipAddress);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public long getViewCount(Long blogId) {
        log.debug("Getting view count for blog ID: {}", blogId);

        // Validate blog exists
        if (!blogRepository.existsById(blogId)) {
            throw BlogNotFoundException.byId(blogId);
        }

        return blogViewRepository.countByBlogId(blogId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUniqueViewCount(Long blogId) {
        log.debug("Getting unique view count for blog ID: {}", blogId);

        // Validate blog exists
        if (!blogRepository.existsById(blogId)) {
            throw BlogNotFoundException.byId(blogId);
        }

        return blogViewRepository.countDistinctIpAddressByBlogId(blogId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogView> getRecentViews(Long blogId, Pageable pageable) {
        log.debug("Getting recent views for blog ID: {}, page: {}, size: {}",
                blogId, pageable.getPageNumber(), pageable.getPageSize());

        // Validate blog exists
        if (!blogRepository.existsById(blogId)) {
            throw BlogNotFoundException.byId(blogId);
        }

        java.util.List<BlogView> views = blogViewRepository.findByBlogIdOrderByViewedAtDesc(blogId, pageable);
        long total = blogViewRepository.countByBlogId(blogId);
        return new PageImpl<>(views, pageable, total);
    }
}
