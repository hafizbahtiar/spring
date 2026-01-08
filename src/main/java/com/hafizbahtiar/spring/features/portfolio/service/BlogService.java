package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for blog management.
 * Handles CRUD operations, publishing, and search functionality for blog posts.
 */
public interface BlogService {

    /**
     * Create a new blog post for a user.
     *
     * @param userId  User ID
     * @param request Blog creation request
     * @return Created BlogResponse
     */
    BlogResponse createBlog(Long userId, BlogRequest request);

    /**
     * Update an existing blog post.
     *
     * @param blogId  Blog ID
     * @param userId  User ID (for ownership validation)
     * @param request Update request
     * @return Updated BlogResponse
     */
    BlogResponse updateBlog(Long blogId, Long userId, BlogRequest request);

    /**
     * Delete a blog post.
     *
     * @param blogId Blog ID
     * @param userId User ID (for ownership validation)
     */
    void deleteBlog(Long blogId, Long userId);

    /**
     * Bulk delete blog posts.
     *
     * @param userId User ID (for ownership validation)
     * @param ids    List of blog IDs to delete
     * @return BulkDeleteResponse with deleted count and failed IDs
     */
    BulkDeleteResponse bulkDeleteBlogs(Long userId, List<Long> ids);

    /**
     * Get blog post by ID.
     *
     * @param blogId Blog ID
     * @param userId User ID (for ownership validation)
     * @return BlogResponse
     */
    BlogResponse getBlog(Long blogId, Long userId);

    /**
     * Get all blog posts for a user with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of BlogResponse
     */
    Page<BlogResponse> getUserBlogs(Long userId, Pageable pageable);

    /**
     * Get published blog posts for a user with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of BlogResponse
     */
    Page<BlogResponse> getPublishedBlogs(Long userId, Pageable pageable);

    /**
     * Get draft blog posts for a user with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of BlogResponse
     */
    Page<BlogResponse> getDraftBlogs(Long userId, Pageable pageable);

    /**
     * Search blog posts by title or content for a user.
     *
     * @param userId     User ID
     * @param searchTerm Search term
     * @param pageable   Pagination parameters
     * @return Page of BlogResponse
     */
    Page<BlogResponse> searchBlogs(Long userId, String searchTerm, Pageable pageable);

    /**
     * Publish a blog post.
     *
     * @param blogId Blog ID
     * @param userId User ID (for ownership validation)
     * @return Updated BlogResponse
     */
    BlogResponse publishBlog(Long blogId, Long userId);

    /**
     * Unpublish a blog post.
     *
     * @param blogId Blog ID
     * @param userId User ID (for ownership validation)
     * @return Updated BlogResponse
     */
    BlogResponse unpublishBlog(Long blogId, Long userId);

    /**
     * Upload cover image for a blog post.
     * Supports both file upload and URL.
     *
     * @param blogId        Blog ID
     * @param userId        User ID (for ownership validation)
     * @param coverImageUrl Cover image URL (optional if file is provided)
     * @param file          Cover image file (optional if coverImageUrl is provided)
     * @return Updated BlogResponse
     */
    BlogResponse uploadCoverImage(Long blogId, Long userId, String coverImageUrl,
            org.springframework.web.multipart.MultipartFile file);
}
