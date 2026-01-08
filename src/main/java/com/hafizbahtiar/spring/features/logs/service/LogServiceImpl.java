package com.hafizbahtiar.spring.features.logs.service;

import com.hafizbahtiar.spring.features.auth.model.AuthLog;
import com.hafizbahtiar.spring.features.auth.repository.mongodb.AuthLogRepository;
import com.hafizbahtiar.spring.features.logs.dto.LogResponse;
import com.hafizbahtiar.spring.features.permissions.model.PermissionLog;
import com.hafizbahtiar.spring.features.permissions.repository.mongodb.PermissionLogRepository;
import com.hafizbahtiar.spring.features.portfolio.model.PortfolioLog;
import com.hafizbahtiar.spring.features.portfolio.repository.mongodb.PortfolioLogRepository;
import com.hafizbahtiar.spring.features.user.model.UserActivity;
import com.hafizbahtiar.spring.features.user.repository.mongodb.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for aggregating logs from multiple MongoDB collections.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogServiceImpl implements LogService {

    private final AuthLogRepository authLogRepository;
    private final UserActivityRepository userActivityRepository;
    private final PortfolioLogRepository portfolioLogRepository;
    private final PermissionLogRepository permissionLogRepository;

    @Override
    public List<LogResponse> getAllLogs(int limit) {
        log.debug("Fetching aggregated logs from all collections with limit: {}", limit);

        List<LogResponse> allLogs = new ArrayList<>();

        // Fetch from each collection
        try {
            List<AuthLog> authLogs = authLogRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "timestamp"));
            allLogs.addAll(authLogs.stream()
                    .map(this::convertAuthLog)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to fetch auth logs: {}", e.getMessage());
        }

        try {
            List<UserActivity> userActivities = userActivityRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "timestamp"));
            allLogs.addAll(userActivities.stream()
                    .map(this::convertUserActivity)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to fetch user activity logs: {}", e.getMessage());
        }

        try {
            List<PortfolioLog> portfolioLogs = portfolioLogRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "timestamp"));
            allLogs.addAll(portfolioLogs.stream()
                    .map(this::convertPortfolioLog)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to fetch portfolio logs: {}", e.getMessage());
        }

        try {
            List<PermissionLog> permissionLogs = permissionLogRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "timestamp"));
            allLogs.addAll(permissionLogs.stream()
                    .map(this::convertPermissionLog)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to fetch permission logs: {}", e.getMessage());
        }

        // Sort by timestamp descending and limit
        return allLogs.stream()
                .sorted(Comparator.comparing(LogResponse::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogResponse> getUserActivityLogs(int limit) {
        log.debug("Fetching user activity logs with limit: {}", limit);

        List<UserActivity> activities = userActivityRepository.findAll(
                Sort.by(Sort.Direction.DESC, "timestamp"));

        return activities.stream()
                .map(this::convertUserActivity)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogResponse> getSecurityLogs(int limit) {
        log.debug("Fetching security logs (auth + permission) with limit: {}", limit);

        List<LogResponse> securityLogs = new ArrayList<>();

        // Fetch auth logs
        try {
            List<AuthLog> authLogs = authLogRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "timestamp"));
            securityLogs.addAll(authLogs.stream()
                    .map(this::convertAuthLog)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to fetch auth logs: {}", e.getMessage());
        }

        // Fetch permission logs
        try {
            List<PermissionLog> permissionLogs = permissionLogRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "timestamp"));
            securityLogs.addAll(permissionLogs.stream()
                    .map(this::convertPermissionLog)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to fetch permission logs: {}", e.getMessage());
        }

        // Sort by timestamp descending and limit
        return securityLogs.stream()
                .sorted(Comparator.comparing(LogResponse::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogResponse> getPortfolioLogs(int limit) {
        log.debug("Fetching portfolio logs with limit: {}", limit);

        List<PortfolioLog> portfolioLogs = portfolioLogRepository.findAll(
                Sort.by(Sort.Direction.DESC, "timestamp"));

        return portfolioLogs.stream()
                .map(this::convertPortfolioLog)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Convert AuthLog to LogResponse.
     */
    private LogResponse convertAuthLog(AuthLog authLog) {
        return LogResponse.builder()
                .id(authLog.getId())
                .logType("AUTH")
                .eventType(authLog.getEventType())
                .userId(authLog.getUserId())
                .timestamp(authLog.getTimestamp())
                .ipAddress(authLog.getIpAddress())
                .userAgent(authLog.getUserAgent())
                .sessionId(authLog.getSessionId())
                .requestId(authLog.getRequestId())
                .success(authLog.getSuccess())
                .failureReason(authLog.getFailureReason())
                .metadata(authLog.getMetadata())
                .responseTimeMs(authLog.getResponseTimeMs())
                .build();
    }

    /**
     * Convert UserActivity to LogResponse.
     */
    private LogResponse convertUserActivity(UserActivity activity) {
        // Build metadata from activity details
        Object metadata = null;
        if (activity.getDetails() != null) {
            metadata = activity.getDetails();
        } else if (activity.getMetadata() != null) {
            metadata = activity.getMetadata();
        }

        return LogResponse.builder()
                .id(activity.getId())
                .logType("USER_ACTIVITY")
                .eventType(activity.getActivityType())
                .userId(activity.getUserId())
                .timestamp(activity.getTimestamp())
                .ipAddress(activity.getDetails() != null ? activity.getDetails().getIpAddress() : null)
                .userAgent(activity.getDetails() != null ? activity.getDetails().getUserAgent() : null)
                .sessionId(activity.getSessionId())
                .requestId(activity.getDetails() != null ? activity.getDetails().getRequestId() : null)
                .success(true) // UserActivity doesn't have success field, assume true
                .failureReason(null)
                .metadata(metadata)
                .responseTimeMs(activity.getDetails() != null ? activity.getDetails().getResponseTimeMs() : null)
                .build();
    }

    /**
     * Convert PortfolioLog to LogResponse.
     */
    private LogResponse convertPortfolioLog(PortfolioLog portfolioLog) {
        // Build metadata with entity-specific fields
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("entityType", portfolioLog.getEntityType());
        metadata.put("entityId", portfolioLog.getEntityId());

        return LogResponse.builder()
                .id(portfolioLog.getId())
                .logType("PORTFOLIO")
                .eventType(portfolioLog.getEventType())
                .userId(portfolioLog.getUserId())
                .timestamp(portfolioLog.getTimestamp())
                .ipAddress(portfolioLog.getIpAddress())
                .userAgent(portfolioLog.getUserAgent())
                .sessionId(portfolioLog.getSessionId())
                .requestId(portfolioLog.getRequestId())
                .success(portfolioLog.getSuccess())
                .failureReason(portfolioLog.getFailureReason())
                .metadata(metadata)
                .responseTimeMs(portfolioLog.getResponseTimeMs())
                .build();
    }

    /**
     * Convert PermissionLog to LogResponse.
     */
    private LogResponse convertPermissionLog(PermissionLog permissionLog) {
        // Build metadata with permission-specific fields
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        if (permissionLog.getGroupId() != null) {
            metadata.put("groupId", permissionLog.getGroupId());
        }
        if (permissionLog.getPermissionId() != null) {
            metadata.put("permissionId", permissionLog.getPermissionId());
        }
        if (permissionLog.getTargetUserId() != null) {
            metadata.put("targetUserId", permissionLog.getTargetUserId());
        }
        if (permissionLog.getDetails() != null) {
            metadata.put("details", permissionLog.getDetails());
        }

        return LogResponse.builder()
                .id(permissionLog.getId())
                .logType("PERMISSION")
                .eventType(permissionLog.getEventType())
                .userId(permissionLog.getUserId())
                .timestamp(permissionLog.getTimestamp())
                .ipAddress(permissionLog.getIpAddress())
                .userAgent(permissionLog.getUserAgent())
                .sessionId(permissionLog.getSessionId())
                .requestId(permissionLog.getRequestId())
                .success(permissionLog.getSuccess())
                .failureReason(permissionLog.getFailureReason())
                .metadata(metadata.isEmpty() ? null : metadata)
                .responseTimeMs(permissionLog.getResponseTimeMs())
                .build();
    }
}

