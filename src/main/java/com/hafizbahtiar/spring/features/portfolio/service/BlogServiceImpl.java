package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.common.util.SlugUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Blog;
import com.hafizbahtiar.spring.features.portfolio.exception.BlogNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.PortfolioException;
import com.hafizbahtiar.spring.features.portfolio.mapper.BlogMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.BlogRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of BlogService.
 * Handles blog CRUD operations, slug generation, publishing, and search
 * functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final UserRepository userRepository;
    private final PortfolioLoggingService portfolioLoggingService;

    @Value("${app.file-storage.upload-dir:uploads/blog-covers}")
    private String uploadDir;

    @Value("${app.file-storage.base-url:http://localhost:8080/api/v1/files/blog-covers}")
    private String baseUrl;

    @Value("${app.file-storage.max-file-size:10485760}") // 10MB default for blog covers
    private long maxFileSize;

    /**
     * Get current HttpServletRequest for logging context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not retrieve current request: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public BlogResponse createBlog(Long userId, BlogRequest request) {
        log.debug("Creating blog post for user ID: {}, title: {}", userId, request.getTitle());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = SlugUtils.generateSlug(request.getTitle());
        } else {
            slug = SlugUtils.generateSlug(slug);
        }

        // Ensure slug is unique
        slug = SlugUtils.generateUniqueSlug(slug, blogRepository::existsBySlug);

        // Map request to entity
        Blog blog = blogMapper.toEntity(request);
        blog.setUser(user);
        blog.setSlug(slug);

        // Handle publishing
        if (Boolean.TRUE.equals(request.getPublished())) {
            blog.publish();
        }

        long startTime = System.currentTimeMillis();
        Blog savedBlog = blogRepository.save(blog);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Blog post created successfully with ID: {}, slug: {}", savedBlog.getId(), savedBlog.getSlug());

        // Log blog creation
        portfolioLoggingService.logBlogCreated(
                savedBlog.getId(),
                userId,
                savedBlog.getTitle(),
                getCurrentRequest(),
                responseTime);

        return blogMapper.toResponse(savedBlog);
    }

    @Override
    public BlogResponse updateBlog(Long blogId, Long userId, BlogRequest request) {
        log.debug("Updating blog post ID: {} for user ID: {}", blogId, userId);

        // Validate blog exists and belongs to user
        Blog blog = blogRepository.findByUserIdAndId(userId, blogId)
                .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

        // Handle slug update
        String newSlug = request.getSlug();
        if (newSlug != null && !newSlug.trim().isEmpty()) {
            newSlug = SlugUtils.generateSlug(newSlug);
            // Check if slug is different and if it conflicts with another blog
            if (!newSlug.equals(blog.getSlug()) && blogRepository.existsBySlugAndIdNot(newSlug, blogId)) {
                throw PortfolioException.invalidInput("Slug already exists: " + newSlug);
            }
            blog.setSlug(newSlug);
        } else if (request.getTitle() != null && !request.getTitle().equals(blog.getTitle())) {
            // Auto-generate slug from new title if title changed and slug not provided
            String generatedSlug = SlugUtils.generateSlug(request.getTitle());
            if (!generatedSlug.equals(blog.getSlug()) && blogRepository.existsBySlugAndIdNot(generatedSlug, blogId)) {
                generatedSlug = SlugUtils.generateUniqueSlug(generatedSlug,
                        s -> blogRepository.existsBySlugAndIdNot(s, blogId));
            }
            blog.setSlug(generatedSlug);
        }

        // Track published status change
        Boolean wasPublished = blog.isPublished();
        Boolean willBePublished = request.getPublished() != null ? request.getPublished() : wasPublished;

        // Handle cover image change - delete old file if it's being replaced or removed
        String oldCoverImage = blog.getCoverImage();
        String newCoverImage = request.getCoverImage();

        // If cover image is being changed or removed, and old one was a local file,
        // delete it
        if (oldCoverImage != null && oldCoverImage.startsWith(baseUrl)) {
            // Check if cover image is being removed (null or empty) or changed to a
            // different URL
            if (newCoverImage == null || newCoverImage.trim().isEmpty() || !newCoverImage.equals(oldCoverImage)) {
                deleteOldCoverImageFile(oldCoverImage);
            }
        }

        // Update entity from request
        blogMapper.updateEntityFromRequest(request, blog);

        // Handle publishing/unpublishing
        if (Boolean.TRUE.equals(willBePublished) && !wasPublished) {
            blog.publish();
        } else if (Boolean.FALSE.equals(willBePublished) && wasPublished) {
            blog.unpublish();
        }

        long startTime = System.currentTimeMillis();
        Blog updatedBlog = blogRepository.save(blog);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Blog post updated successfully with ID: {}", updatedBlog.getId());

        // Log blog update
        portfolioLoggingService.logBlogUpdated(
                updatedBlog.getId(),
                userId,
                updatedBlog.getTitle(),
                getCurrentRequest(),
                responseTime);

        return blogMapper.toResponse(updatedBlog);
    }

    @Override
    public void deleteBlog(Long blogId, Long userId) {
        log.debug("Deleting blog post ID: {} for user ID: {}", blogId, userId);

        // Validate blog exists and belongs to user
        Blog blog = blogRepository.findByUserIdAndId(userId, blogId)
                .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

        String title = blog.getTitle();
        blogRepository.delete(blog);
        log.info("Blog post deleted successfully with ID: {}", blogId);

        // Log blog deletion
        portfolioLoggingService.logBlogDeleted(blogId, userId, title, getCurrentRequest());
    }

    @Override
    @Transactional
    public BulkDeleteResponse bulkDeleteBlogs(Long userId, List<Long> ids) {
        log.debug("Bulk deleting blog posts for user ID: {}, IDs: {}", userId, ids);

        List<Long> failedIds = new ArrayList<>();
        int deletedCount = 0;

        for (Long blogId : ids) {
            try {
                // Validate blog exists and belongs to user
                Blog blog = blogRepository.findByUserIdAndId(userId, blogId)
                        .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

                String title = blog.getTitle();
                blogRepository.delete(blog);
                deletedCount++;

                // Log individual blog deletion
                portfolioLoggingService.logBlogDeleted(blogId, userId, title, getCurrentRequest());
            } catch (Exception e) {
                log.warn("Failed to delete blog post ID: {} for user ID: {} - {}", blogId, userId, e.getMessage());
                failedIds.add(blogId);
            }
        }

        log.info("Bulk delete completed for user ID: {} - Deleted: {}, Failed: {}", userId, deletedCount,
                failedIds.size());

        if (failedIds.isEmpty()) {
            return BulkDeleteResponse.success(deletedCount);
        } else {
            return BulkDeleteResponse.withFailures(deletedCount, failedIds);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponse getBlog(Long blogId, Long userId) {
        log.debug("Fetching blog post ID: {} for user ID: {}", blogId, userId);

        Blog blog = blogRepository.findByUserIdAndId(userId, blogId)
                .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

        return blogMapper.toResponse(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getUserBlogs(Long userId, Pageable pageable) {
        log.debug("Fetching all blog posts for user ID: {} with pagination: {}", userId, pageable);
        // Ensure DESC ordering by createdAt if no explicit sort is provided
        Pageable sortedPageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Blog> blogs = blogRepository.findByUserId(userId, sortedPageable);
        return blogs.map(blogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getPublishedBlogs(Long userId, Pageable pageable) {
        log.debug("Fetching published blog posts for user ID: {} with pagination: {}", userId, pageable);
        Page<Blog> blogs = blogRepository.findByUserIdAndPublishedTrue(userId, pageable);
        return blogs.map(blogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getDraftBlogs(Long userId, Pageable pageable) {
        log.debug("Fetching draft blog posts for user ID: {} with pagination: {}", userId, pageable);
        Page<Blog> blogs = blogRepository.findByUserIdAndPublishedFalse(userId, pageable);
        return blogs.map(blogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> searchBlogs(Long userId, String searchTerm, Pageable pageable) {
        log.debug("Searching blog posts for user ID: {} with term: {}", userId, searchTerm);
        Page<Blog> blogs = blogRepository.searchByUserIdAndSearchTerm(userId, searchTerm, pageable);
        return blogs.map(blogMapper::toResponse);
    }

    @Override
    public BlogResponse publishBlog(Long blogId, Long userId) {
        log.debug("Publishing blog post ID: {} for user ID: {}", blogId, userId);

        Blog blog = blogRepository.findByUserIdAndId(userId, blogId)
                .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

        blog.publish();

        long startTime = System.currentTimeMillis();
        Blog updatedBlog = blogRepository.save(blog);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Blog post published successfully with ID: {}", updatedBlog.getId());

        // Log blog update
        portfolioLoggingService.logBlogUpdated(
                updatedBlog.getId(),
                userId,
                updatedBlog.getTitle(),
                getCurrentRequest(),
                responseTime);

        return blogMapper.toResponse(updatedBlog);
    }

    @Override
    public BlogResponse unpublishBlog(Long blogId, Long userId) {
        log.debug("Unpublishing blog post ID: {} for user ID: {}", blogId, userId);

        Blog blog = blogRepository.findByUserIdAndId(userId, blogId)
                .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

        blog.unpublish();

        long startTime = System.currentTimeMillis();
        Blog updatedBlog = blogRepository.save(blog);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Blog post unpublished successfully with ID: {}", updatedBlog.getId());

        // Log blog update
        portfolioLoggingService.logBlogUpdated(
                updatedBlog.getId(),
                userId,
                updatedBlog.getTitle(),
                getCurrentRequest(),
                responseTime);

        return blogMapper.toResponse(updatedBlog);
    }

    @Override
    public BlogResponse uploadCoverImage(Long blogId, Long userId, String coverImageUrl, MultipartFile file) {
        log.info("Cover image upload request received for blog ID: {}, user ID: {}", blogId, userId);

        // Validate blog exists and belongs to user
        Blog blog = blogRepository.findByUserIdAndId(userId, blogId)
                .orElseThrow(() -> BlogNotFoundException.byIdAndUser(blogId, userId));

        // Delete old cover image file if it exists and is a local file
        String oldCoverImage = blog.getCoverImage();
        if (oldCoverImage != null && oldCoverImage.startsWith(baseUrl)) {
            deleteOldCoverImageFile(oldCoverImage);
        }

        String finalCoverImageUrl = null;

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            // Validate file
            validateCoverImageFile(file);

            // Store file and get URL
            finalCoverImageUrl = storeCoverImageFile(blogId, file);
            log.info("Cover image file stored successfully for blog ID: {}, URL: {}", blogId, finalCoverImageUrl);
        } else if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
            // Use provided URL
            finalCoverImageUrl = coverImageUrl.trim();
            log.info("Cover image URL provided for blog ID: {}, URL: {}", blogId, finalCoverImageUrl);
        } else {
            throw new IllegalArgumentException("Either coverImageUrl or file must be provided");
        }

        // Update blog's cover image URL
        blog.setCoverImage(finalCoverImageUrl);
        Blog updatedBlog = blogRepository.save(blog);
        log.info("Cover image updated successfully for blog ID: {}", blogId);

        // Log blog update
        portfolioLoggingService.logBlogUpdated(
                updatedBlog.getId(),
                userId,
                updatedBlog.getTitle(),
                getCurrentRequest(),
                0L);

        return blogMapper.toResponse(updatedBlog);
    }

    /**
     * Validate cover image file (type and size)
     */
    private void validateCoverImageFile(MultipartFile file) {
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }

        // Check file type (images only)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Check specific image types
        String[] allowedTypes = { "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp" };
        boolean isAllowed = false;
        for (String allowedType : allowedTypes) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException(
                    "File type not allowed. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }

    /**
     * Store cover image file and return the URL
     */
    private String storeCoverImageFile(Long blogId, MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = "blog_" + blogId + "_" + UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL
            return baseUrl + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store cover image file for blog ID: {}", blogId, e);
            throw new RuntimeException("Failed to store cover image file", e);
        }
    }

    /**
     * Delete old cover image file from filesystem
     */
    private void deleteOldCoverImageFile(String coverImageUrl) {
        try {
            // Extract filename from URL
            // URL format: http://localhost:8080/api/v1/files/blog-covers/filename
            String filename = coverImageUrl.substring(coverImageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted old cover image file: {}", filename);
            } else {
                log.debug("Old cover image file not found: {}", filename);
            }
        } catch (IOException e) {
            log.warn("Failed to delete old cover image file: {}", coverImageUrl, e);
            // Don't throw exception - file deletion failure shouldn't block the update
        }
    }
}
