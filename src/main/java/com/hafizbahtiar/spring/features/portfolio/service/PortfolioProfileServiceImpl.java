package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioProfileRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioProfileResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.PortfolioProfile;
import com.hafizbahtiar.spring.features.portfolio.mapper.PortfolioProfileMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.PortfolioProfileRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Implementation of PortfolioProfileService.
 * Handles portfolio profile CRUD operations with automatic profile creation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PortfolioProfileServiceImpl implements PortfolioProfileService {

    private final PortfolioProfileRepository portfolioProfileRepository;
    private final PortfolioProfileMapper portfolioProfileMapper;
    private final UserRepository userRepository;
    private final PortfolioLoggingService portfolioLoggingService;

    /**
     * Get current HttpServletRequest for logging context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("Could not get current request: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public PortfolioProfileResponse getPortfolioProfile(Long userId) {
        log.debug("Fetching portfolio profile for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create profile
        PortfolioProfile profile = portfolioProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating default portfolio profile for user ID: {}", userId);
                    PortfolioProfile newProfile = new PortfolioProfile();
                    newProfile.setUser(user);
                    return portfolioProfileRepository.save(newProfile);
                });

        return portfolioProfileMapper.toResponse(profile);
    }

    @Override
    public PortfolioProfileResponse updatePortfolioProfile(Long userId, PortfolioProfileRequest request) {
        log.debug("Updating portfolio profile for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create profile
        PortfolioProfile profile = portfolioProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new portfolio profile for user ID: {}", userId);
                    PortfolioProfile newProfile = new PortfolioProfile();
                    newProfile.setUser(user);
                    return portfolioProfileRepository.save(newProfile);
                });

        // Update profile from request (only non-null fields)
        portfolioProfileMapper.updateEntityFromRequest(request, profile);

        long startTime = System.currentTimeMillis();
        PortfolioProfile updatedProfile = portfolioProfileRepository.save(profile);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Portfolio profile updated successfully for user ID: {}", userId);

        // Log profile update
        portfolioLoggingService.logPortfolioProfileUpdated(
                updatedProfile.getId(),
                userId,
                getCurrentRequest(),
                responseTime);

        return portfolioProfileMapper.toResponse(updatedProfile);
    }

    @Override
    public PortfolioProfileResponse uploadAvatar(Long userId, String avatarUrl) {
        log.debug("Updating avatar URL for user ID: {}, avatarUrl: {}", userId, avatarUrl);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create profile
        PortfolioProfile profile = portfolioProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new portfolio profile for user ID: {}", userId);
                    PortfolioProfile newProfile = new PortfolioProfile();
                    newProfile.setUser(user);
                    return portfolioProfileRepository.save(newProfile);
                });

        // Update avatar URL
        profile.setAvatarUrl(avatarUrl);

        long startTime = System.currentTimeMillis();
        PortfolioProfile updatedProfile = portfolioProfileRepository.save(profile);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Avatar URL updated successfully for user ID: {}", userId);

        // Log avatar update
        portfolioLoggingService.logPortfolioProfileUpdated(
                updatedProfile.getId(),
                userId,
                getCurrentRequest(),
                responseTime);

        return portfolioProfileMapper.toResponse(updatedProfile);
    }

    @Override
    public PortfolioProfileResponse uploadResume(Long userId, String resumeUrl) {
        log.debug("Updating resume URL for user ID: {}, resumeUrl: {}", userId, resumeUrl);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create profile
        PortfolioProfile profile = portfolioProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new portfolio profile for user ID: {}", userId);
                    PortfolioProfile newProfile = new PortfolioProfile();
                    newProfile.setUser(user);
                    return portfolioProfileRepository.save(newProfile);
                });

        // Update resume URL
        profile.setResumeUrl(resumeUrl);

        long startTime = System.currentTimeMillis();
        PortfolioProfile updatedProfile = portfolioProfileRepository.save(profile);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Resume URL updated successfully for user ID: {}", userId);

        // Log resume update
        portfolioLoggingService.logPortfolioProfileUpdated(
                updatedProfile.getId(),
                userId,
                getCurrentRequest(),
                responseTime);

        return portfolioProfileMapper.toResponse(updatedProfile);
    }
}
