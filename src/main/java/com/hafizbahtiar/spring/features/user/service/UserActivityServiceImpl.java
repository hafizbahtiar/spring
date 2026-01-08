package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.UserActivityResponse;
import com.hafizbahtiar.spring.features.user.model.UserActivity;
import com.hafizbahtiar.spring.features.user.repository.mongodb.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for retrieving user activity logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityRepository userActivityRepository;

    @Override
    public List<UserActivityResponse> getRecentActivities(Long userId, int limit) {
        log.debug("Fetching recent activities for user ID: {} with limit: {}", userId, limit);

        List<UserActivity> activities = userActivityRepository.findByUserIdOrderByTimestampDesc(userId);

        // Limit manually since repository method doesn't support Pageable
        List<UserActivity> limitedActivities = activities.stream()
                .limit(limit)
                .collect(Collectors.toList());

        return limitedActivities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserActivityResponse> getActivitiesByType(Long userId, String activityType, int limit) {
        log.debug("Fetching activities for user ID: {} with type: {} and limit: {}", userId, activityType, limit);

        List<UserActivity> activities = userActivityRepository.findByUserIdAndActivityTypeOrderByTimestampDesc(userId,
                activityType);

        // Limit manually since repository method doesn't support Pageable
        List<UserActivity> limitedActivities = activities.stream()
                .limit(limit)
                .collect(Collectors.toList());

        return limitedActivities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert UserActivity entity to UserActivityResponse DTO.
     */
    private UserActivityResponse toResponse(UserActivity activity) {
        UserActivityResponse.ActivityDetailsResponse detailsResponse = null;
        if (activity.getDetails() != null) {
            UserActivity.ActivityDetails details = activity.getDetails();
            detailsResponse = UserActivityResponse.ActivityDetailsResponse.builder()
                    .endpoint(details.getEndpoint())
                    .method(details.getMethod())
                    .responseStatus(details.getResponseStatus())
                    .responseTimeMs(details.getResponseTimeMs())
                    .userAgent(details.getUserAgent())
                    .ipAddress(details.getIpAddress())
                    .requestId(details.getRequestId())
                    .additionalData(details.getAdditionalData())
                    .build();
        }

        return UserActivityResponse.builder()
                .id(activity.getId())
                .userId(activity.getUserId())
                .sessionId(activity.getSessionId())
                .activityType(activity.getActivityType())
                .timestamp(activity.getTimestamp())
                .details(detailsResponse)
                .metadata(activity.getMetadata())
                .build();
    }
}
