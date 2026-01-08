package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.dto.AuthLogResponse;
import com.hafizbahtiar.spring.features.auth.model.AuthLog;
import com.hafizbahtiar.spring.features.auth.repository.mongodb.AuthLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AuthLogService.
 * Provides methods to retrieve authentication event logs from MongoDB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthLogServiceImpl implements AuthLogService {

    private final AuthLogRepository authLogRepository;

    @Override
    public List<AuthLogResponse> getUserAuthLogs(Long userId, int limit) {
        log.debug("Fetching auth logs for user ID: {}, limit: {}", userId, limit);

        List<AuthLog> logs = authLogRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .limit(Math.min(limit, 100)) // Max 100 logs
                .toList();

        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthLogResponse> getSecurityLogs(int limit) {
        log.debug("Fetching security logs, limit: {}", limit);

        // Get failed login attempts and token invalidations
        List<AuthLog> failedLogins = authLogRepository.findByEventTypeOrderByTimestampDesc("LOGIN_FAILURE")
                .stream()
                .limit(limit / 2)
                .toList();

        List<AuthLog> tokenInvalidations = authLogRepository.findByEventTypeOrderByTimestampDesc("TOKEN_INVALID")
                .stream()
                .limit(limit / 2)
                .toList();

        List<AuthLog> tokenRefreshFailures = authLogRepository
                .findByEventTypeOrderByTimestampDesc("TOKEN_REFRESH_FAILURE")
                .stream()
                .limit(limit / 3)
                .toList();

        // Combine and sort by timestamp descending
        List<AuthLog> allSecurityLogs = new java.util.ArrayList<>();
        allSecurityLogs.addAll(failedLogins);
        allSecurityLogs.addAll(tokenInvalidations);
        allSecurityLogs.addAll(tokenRefreshFailures);

        return allSecurityLogs.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthLogResponse> getFailedLoginAttempts(String identifier, int limit) {
        log.debug("Fetching failed login attempts for identifier: {}, limit: {}", identifier, limit);

        List<AuthLog> logs = authLogRepository.findByIdentifierAndSuccessFalseOrderByTimestampDesc(identifier)
                .stream()
                .limit(Math.min(limit, 100)) // Max 100 logs
                .toList();

        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map AuthLog entity to AuthLogResponse DTO
     */
    private AuthLogResponse mapToResponse(AuthLog log) {
        return AuthLogResponse.builder()
                .id(log.getId())
                .eventType(log.getEventType())
                .userId(log.getUserId())
                .identifier(log.getIdentifier())
                .timestamp(log.getTimestamp())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .sessionId(log.getSessionId())
                .requestId(log.getRequestId())
                .success(log.getSuccess())
                .failureReason(log.getFailureReason())
                .responseTimeMs(log.getResponseTimeMs())
                .tokenExpiresAt(log.getTokenExpiresAt())
                .build();
    }
}
