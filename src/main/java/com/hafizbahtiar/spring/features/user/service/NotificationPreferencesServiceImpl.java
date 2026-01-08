package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.NotificationPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.NotificationPreferencesResponse;
import com.hafizbahtiar.spring.features.user.entity.NotificationPreferences;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.mapper.NotificationPreferencesMapper;
import com.hafizbahtiar.spring.features.user.repository.NotificationPreferencesRepository;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Implementation of NotificationPreferencesService.
 * Handles notification preferences CRUD operations with automatic preferences
 * creation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationPreferencesServiceImpl implements NotificationPreferencesService {

    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final NotificationPreferencesMapper notificationPreferencesMapper;
    private final UserRepository userRepository;
    private final UserActivityLoggingService userActivityLoggingService;

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
    public NotificationPreferencesResponse getNotificationPreferences(Long userId) {
        log.debug("Fetching notification preferences for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create preferences
        NotificationPreferences preferences = notificationPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating default notification preferences for user ID: {}", userId);
                    NotificationPreferences newPreferences = new NotificationPreferences(user);
                    return notificationPreferencesRepository.save(newPreferences);
                });

        return notificationPreferencesMapper.toResponse(preferences);
    }

    @Override
    public NotificationPreferencesResponse updateNotificationPreferences(Long userId,
            NotificationPreferencesRequest request) {
        log.debug("Updating notification preferences for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create preferences
        NotificationPreferences preferences = notificationPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new notification preferences for user ID: {}", userId);
                    NotificationPreferences newPreferences = new NotificationPreferences(user);
                    return notificationPreferencesRepository.save(newPreferences);
                });

        // Update preferences from request (only non-null fields)
        notificationPreferencesMapper.updateEntityFromRequest(request, preferences);

        long startTime = System.currentTimeMillis();
        NotificationPreferences updatedPreferences = notificationPreferencesRepository.save(preferences);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Notification preferences updated successfully for user ID: {}", userId);

        // Log preferences update
        userActivityLoggingService.logNotificationPreferencesUpdated(
                updatedPreferences.getId(),
                userId,
                getCurrentRequest(),
                responseTime);

        return notificationPreferencesMapper.toResponse(updatedPreferences);
    }

    @Override
    public NotificationPreferencesResponse resetNotificationPreferences(Long userId) {
        log.debug("Resetting notification preferences to defaults for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create preferences
        NotificationPreferences preferences = notificationPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new notification preferences for user ID: {}", userId);
                    NotificationPreferences newPreferences = new NotificationPreferences(user);
                    return notificationPreferencesRepository.save(newPreferences);
                });

        // Reset to default values
        preferences.setEmailAccountActivity(true);
        preferences.setEmailSecurityAlerts(true);
        preferences.setEmailMarketing(false);
        preferences.setEmailWeeklyDigest(false);
        preferences.setInAppSystem(true);
        preferences.setInAppProjects(true);
        preferences.setInAppMentions(true);
        preferences.setPushEnabled(false);
        preferences.setPushBrowser(true);
        preferences.setPushMobile(false);

        long startTime = System.currentTimeMillis();
        NotificationPreferences resetPreferences = notificationPreferencesRepository.save(preferences);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Notification preferences reset to defaults for user ID: {}", userId);

        // Log preferences reset
        userActivityLoggingService.logNotificationPreferencesUpdated(
                resetPreferences.getId(),
                userId,
                getCurrentRequest(),
                responseTime);

        return notificationPreferencesMapper.toResponse(resetPreferences);
    }
}
