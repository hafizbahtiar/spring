package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.BulkDeleteRequest;
import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogResponse;
import com.hafizbahtiar.spring.features.portfolio.service.BlogService;
import jakarta.validation.Valid;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for blog management endpoints.
 * Handles blog CRUD operations, publishing, and search functionality.
 */
@RestController
@RequestMapping("/api/v1/portfolio/blogs")
@RequiredArgsConstructor
@Slf4j
public class BlogController {

    private final BlogService blogService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Create a new blog post
     * POST /api/v1/portfolio/blogs
     * Requires: OWNER/ADMIN role OR portfolio.blog page WRITE permission
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'WRITE')")
    public ResponseEntity<ApiResponse<BlogResponse>> createBlog(@Valid @RequestBody BlogRequest request) {
        Long userId = getCurrentUserId();
        log.info("Blog creation request received for user ID: {}, title: {}", userId, request.getTitle());
        BlogResponse response = blogService.createBlog(userId, request);
        return ResponseUtils.created(response, "Blog post created successfully");
    }

    /**
     * Get all blog posts for current user with pagination
     * GET /api/v1/portfolio/blogs
     * Requires: OWNER/ADMIN role OR portfolio.blog page READ permission
     * Query params: published (boolean), page, size, sort
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'READ')")
    public ResponseEntity<ApiResponse<PaginatedResponse<BlogResponse>>> getUserBlogs(
            @RequestParam(required = false) Boolean published,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Fetching blog posts for user ID: {}, published: {}, page: {}, size: {}",
                userId, published, pageable.getPageNumber(), pageable.getPageSize());

        Page<BlogResponse> blogs;
        if (published != null && published) {
            blogs = blogService.getPublishedBlogs(userId, pageable);
        } else if (published != null && !published) {
            blogs = blogService.getDraftBlogs(userId, pageable);
        } else {
            blogs = blogService.getUserBlogs(userId, pageable);
        }

        return ResponseUtils.okPage(blogs);
    }

    /**
     * Get blog post by ID
     * GET /api/v1/portfolio/blogs/{id}
     * Requires: OWNER/ADMIN role OR portfolio.blog page READ permission
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'READ')")
    public ResponseEntity<ApiResponse<BlogResponse>> getBlog(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching blog post ID: {} for user ID: {}", id, userId);
        BlogResponse response = blogService.getBlog(id, userId);
        return ResponseUtils.ok(response);
    }

    /**
     * Update blog post
     * PUT /api/v1/portfolio/blogs/{id}
     * Requires: OWNER/ADMIN role OR portfolio.blog page WRITE permission
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'WRITE')")
    public ResponseEntity<ApiResponse<BlogResponse>> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogRequest request) {
        Long userId = getCurrentUserId();
        log.info("Blog update request received for blog ID: {}, user ID: {}", id, userId);
        BlogResponse response = blogService.updateBlog(id, userId, request);
        return ResponseUtils.ok(response, "Blog post updated successfully");
    }

    /**
     * Delete blog post
     * DELETE /api/v1/portfolio/blogs/{id}
     * Requires: OWNER/ADMIN role OR portfolio.blog page DELETE permission
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteBlog(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Blog deletion request received for blog ID: {}, user ID: {}", id, userId);
        blogService.deleteBlog(id, userId);
        return ResponseUtils.noContent();
    }

    /**
     * Bulk delete blog posts
     * POST /api/v1/portfolio/blogs/bulk-delete
     * Requires: OWNER/ADMIN role OR portfolio.blog page DELETE permission
     * Body: BulkDeleteRequest with list of blog IDs
     */
    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'DELETE')")
    public ResponseEntity<ApiResponse<BulkDeleteResponse>> bulkDeleteBlogs(
            @Valid @RequestBody BulkDeleteRequest request) {
        Long userId = getCurrentUserId();
        log.info("Bulk delete blog posts request received for user ID: {}, IDs: {}", userId, request.getIds());
        BulkDeleteResponse response = blogService.bulkDeleteBlogs(userId, request.getIds());
        return ResponseUtils.ok(response, "Bulk delete completed");
    }

    /**
     * Search blog posts
     * GET /api/v1/portfolio/blogs/search
     * Requires: OWNER/ADMIN role OR portfolio.blog page READ permission
     * Query params: q (search term), page, size
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'READ')")
    public ResponseEntity<ApiResponse<PaginatedResponse<BlogResponse>>> searchBlogs(
            @RequestParam String q,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Searching blog posts for user ID: {} with query: {}", userId, q);
        Page<BlogResponse> blogs = blogService.searchBlogs(userId, q, pageable);
        return ResponseUtils.okPage(blogs);
    }

    /**
     * Publish blog post
     * PUT /api/v1/portfolio/blogs/{id}/publish
     * Requires: OWNER/ADMIN role OR portfolio.blog page WRITE permission
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'WRITE')")
    public ResponseEntity<ApiResponse<BlogResponse>> publishBlog(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Publish blog post request received for blog ID: {}, user ID: {}", id, userId);
        BlogResponse response = blogService.publishBlog(id, userId);
        return ResponseUtils.ok(response, "Blog post published successfully");
    }

    /**
     * Unpublish blog post
     * PUT /api/v1/portfolio/blogs/{id}/unpublish
     * Requires: User owns the blog post OR ADMIN role
     */
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.blog', 'WRITE')")
    public ResponseEntity<ApiResponse<BlogResponse>> unpublishBlog(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Unpublish blog post request received for blog ID: {}, user ID: {}", id, userId);
        BlogResponse response = blogService.unpublishBlog(id, userId);
        return ResponseUtils.ok(response, "Blog post unpublished successfully");
    }

    /**
     * Upload cover image for a blog post
     * POST /api/v1/portfolio/blogs/{id}/cover-image
     * Requires: User owns the blog post OR ADMIN role
     * Supports two methods:
     * 1. File upload: multipart/form-data with "file" field
     * 2. URL upload: multipart/form-data with "coverImageUrl" field
     * Returns: Updated BlogResponse
     */
    @PostMapping("/{id}/cover-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BlogResponse>> uploadCoverImage(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String coverImageUrl) {
        Long userId = getCurrentUserId();
        log.info("Cover image upload request received for blog ID: {}, user ID: {}", id, userId);

        // Validate that at least one is provided
        if ((file == null || file.isEmpty()) && (coverImageUrl == null || coverImageUrl.trim().isEmpty())) {
            throw new IllegalArgumentException("Either 'file' or 'coverImageUrl' must be provided");
        }

        BlogResponse response = blogService.uploadCoverImage(id, userId, coverImageUrl, file);
        return ResponseUtils.ok(response, "Cover image updated successfully");
    }
}
