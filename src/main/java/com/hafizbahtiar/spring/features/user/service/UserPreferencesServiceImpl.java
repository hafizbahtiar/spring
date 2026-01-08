package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.UserPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.UserPreferencesResponse;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.entity.UserPreferences;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.mapper.UserPreferencesMapper;
import com.hafizbahtiar.spring.features.user.repository.UserPreferencesRepository;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Implementation of UserPreferencesService.
 * Handles user preferences CRUD operations with automatic preferences creation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPreferencesServiceImpl implements UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;
    private final UserPreferencesMapper userPreferencesMapper;
    private final UserRepository userRepository;
    private final UserActivityLoggingService userActivityLoggingService;
    
    // Self-injection to enable transaction proxy for createDefaultPreferences
    // This allows the @Transactional annotation to work when called from within the same class
    @Autowired
    @Lazy
    private UserPreferencesServiceImpl self;

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
    @Transactional(readOnly = true)
    public UserPreferencesResponse getUserPreferences(Long userId) {
        log.debug("Fetching user preferences for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get preferences (read-only)
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // If preferences don't exist, create them in a separate write transaction
                    // Use self-injection to ensure transaction proxy is used
                    log.info("Creating default user preferences for user ID: {}", userId);
                    return self.createDefaultPreferences(user);
                });

        return userPreferencesMapper.toResponse(preferences);
    }

    /**
     * Create default user preferences in a separate write transaction.
     * This method is called when preferences don't exist and need to be created.
     * Uses REQUIRES_NEW propagation to ensure a new write transaction is created
     * even when called from a read-only transaction context.
     * Must be called through self-injection (self.createDefaultPreferences) to work properly.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserPreferences createDefaultPreferences(User user) {
        UserPreferences newPreferences = new UserPreferences(user);
        return userPreferencesRepository.save(newPreferences);
    }

    @Override
    public UserPreferencesResponse updateUserPreferences(Long userId, UserPreferencesRequest request) {
        log.debug("Updating user preferences for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create preferences
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new user preferences for user ID: {}", userId);
                    UserPreferences newPreferences = new UserPreferences(user);
                    return userPreferencesRepository.save(newPreferences);
                });

        // Update preferences from request (only non-null fields)
        userPreferencesMapper.updateEntityFromRequest(request, preferences);

        long startTime = System.currentTimeMillis();
        UserPreferences updatedPreferences = userPreferencesRepository.save(preferences);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("User preferences updated successfully for user ID: {}", userId);

        // Log preferences update
        userActivityLoggingService.logPreferencesUpdated(
                updatedPreferences.getId(),
                userId,
                getCurrentRequest(),
                responseTime);

        return userPreferencesMapper.toResponse(updatedPreferences);
    }

    @Override
    public UserPreferencesResponse resetUserPreferences(Long userId) {
        log.debug("Resetting user preferences to defaults for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create preferences
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new user preferences for user ID: {}", userId);
                    UserPreferences newPreferences = new UserPreferences(user);
                    return userPreferencesRepository.save(newPreferences);
                });

        // Reset to default values
        preferences.setTheme("system");
        preferences.setLanguage("en");
        preferences.setDateFormat("MM/DD/YYYY");
        preferences.setTimeFormat("12h");
        preferences.setTimezone("UTC");
        preferences.setDefaultDashboardView("grid");
        preferences.setItemsPerPage("20");
        preferences.setShowWidgets(true);
        preferences.setEditorTheme("dark");
        preferences.setEditorFontSize(14);
        preferences.setEditorLineHeight(1.5);
        preferences.setEditorTabSize(4);

        long startTime = System.currentTimeMillis();
        UserPreferences resetPreferences = userPreferencesRepository.save(preferences);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("User preferences reset to defaults for user ID: {}", userId);

        // Log preferences reset
        userActivityLoggingService.logPreferencesUpdated(
                resetPreferences.getId(),
                userId,
                getCurrentRequest(),
                responseTime);

        return userPreferencesMapper.toResponse(resetPreferences);
    }
}

