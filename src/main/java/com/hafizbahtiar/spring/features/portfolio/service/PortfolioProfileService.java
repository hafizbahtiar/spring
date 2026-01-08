package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioProfileRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioProfileResponse;

/**
 * Service interface for portfolio profile management.
 * Handles CRUD operations for user's portfolio profile.
 */
public interface PortfolioProfileService {

    /**
     * Get portfolio profile for a user.
     * Creates a default profile if one doesn't exist.
     *
     * @param userId User ID
     * @return PortfolioProfileResponse
     */
    PortfolioProfileResponse getPortfolioProfile(Long userId);

    /**
     * Update portfolio profile for a user.
     * Creates a profile if one doesn't exist.
     *
     * @param userId  User ID
     * @param request Update request (all fields optional for partial updates)
     * @return Updated PortfolioProfileResponse
     */
    PortfolioProfileResponse updatePortfolioProfile(Long userId, PortfolioProfileRequest request);

    /**
     * Update avatar URL for a user's portfolio profile.
     *
     * @param userId    User ID
     * @param avatarUrl Avatar URL
     * @return Updated PortfolioProfileResponse
     */
    PortfolioProfileResponse uploadAvatar(Long userId, String avatarUrl);

    /**
     * Update resume URL for a user's portfolio profile.
     *
     * @param userId    User ID
     * @param resumeUrl Resume URL
     * @return Updated PortfolioProfileResponse
     */
    PortfolioProfileResponse uploadResume(Long userId, String resumeUrl);
}
