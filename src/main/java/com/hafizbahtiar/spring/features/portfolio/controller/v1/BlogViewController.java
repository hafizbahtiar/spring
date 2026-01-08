package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogViewResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogViewStatisticsResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.BlogView;
import com.hafizbahtiar.spring.features.portfolio.exception.BlogNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.repository.BlogRepository;
import com.hafizbahtiar.spring.features.portfolio.service.BlogViewService;
import com.hafizbahtiar.spring.features.portfolio.service.PortfolioLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for blog view tracking endpoints.
 * Handles view tracking, statistics, and view history for blog posts.
 */
@RestController
@RequestMapping("/api/v1/portfolio/blogs/{blogId}/views")
@RequiredArgsConstructor
@Slf4j
public class BlogViewController {

    private final BlogViewService blogViewService;
    private final BlogRepository blogRepository;
    private final PortfolioLoggingService portfolioLoggingService;

    /**
     * Track a view for a blog post.
     * POST /api/v1/portfolio/blogs/{blogId}/views
     * Public endpoint - no authentication required
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> trackView(
            @PathVariable Long blogId,
            HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("View tracking request for blog ID: {}", blogId);

        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        Long userId = getCurrentUserIdIfAuthenticated();

        boolean tracked = blogViewService.trackView(blogId, ipAddress, userAgent, userId);
        long responseTime = System.currentTimeMillis() - startTime;

        // Log view tracking event
        portfolioLoggingService.logBlogViewTracked(blogId, userId, tracked, request, responseTime);

        if (tracked) {
            log.info("View tracked for blog ID: {}, IP: {}", blogId, ipAddress);
            return ResponseUtils.ok(true, "View tracked successfully");
        } else {
            log.debug("Duplicate view (within 24 hours) for blog ID: {}, IP: {}", blogId, ipAddress);
            return ResponseUtils.ok(false, "View already tracked within 24 hours");
        }
    }

    /**
     * Get view statistics for a blog post.
     * GET /api/v1/portfolio/blogs/{blogId}/views
     * Requires: Authenticated user (owner only)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BlogViewStatisticsResponse>> getViewStatistics(
            @PathVariable Long blogId) {
        log.debug("Getting view statistics for blog ID: {}", blogId);

        Long userId = getCurrentUserId();

        // Validate blog exists and user owns it
        blogRepository.findByUserIdAndId(userId, blogId)
                .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

        long viewCount = blogViewService.getViewCount(blogId);
        long uniqueViewCount = blogViewService.getUniqueViewCount(blogId);

        BlogViewStatisticsResponse statistics = BlogViewStatisticsResponse.builder()
                .blogId(blogId)
                .viewCount(viewCount)
                .uniqueViewCount(uniqueViewCount)
                .build();

        return ResponseUtils.ok(statistics);
    }

    /**
     * Get recent views for a blog post.
     * GET /api/v1/portfolio/blogs/{blogId}/views/recent
     * Requires: Authenticated user (owner only)
     */
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginatedResponse<BlogViewResponse>>> getRecentViews(
            @PathVariable Long blogId,
            @PageableDefault(size = 20, sort = "viewedAt") Pageable pageable) {
        log.debug("Getting recent views for blog ID: {}, page: {}, size: {}",
                blogId, pageable.getPageNumber(), pageable.getPageSize());

        Long userId = getCurrentUserId();

        // Validate blog exists and user owns it
        blogRepository.findByUserIdAndId(userId, blogId)
                .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

        Page<BlogView> views = blogViewService.getRecentViews(blogId, pageable);

        Page<BlogViewResponse> viewResponses = views.map(this::mapToResponse);

        return ResponseUtils.okPage(viewResponses);
    }

    /**
     * Get current authenticated user ID if available
     */
    private Long getCurrentUserIdIfAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
                return userPrincipal.getId();
            }
        } catch (Exception e) {
            log.debug("User not authenticated or error getting user ID: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get current authenticated user ID (throws exception if not authenticated)
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Map BlogView entity to BlogViewResponse DTO
     */
    private BlogViewResponse mapToResponse(BlogView blogView) {
        return BlogViewResponse.builder()
                .id(blogView.getId())
                .blogId(blogView.getBlog().getId())
                .ipAddress(blogView.getIpAddress())
                .userAgent(blogView.getUserAgent())
                .userId(blogView.getUser() != null ? blogView.getUser().getId() : null)
                .viewedAt(blogView.getViewedAt())
                .build();
    }
}
