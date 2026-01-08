package com.hafizbahtiar.spring.features.permissions.service;

import com.hafizbahtiar.spring.features.permissions.model.PermissionLog;
import com.hafizbahtiar.spring.features.permissions.repository.mongodb.PermissionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for logging permission events to MongoDB.
 * Provides methods to log various permission-related events for audit and
 * security purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionLoggingService {

    private final PermissionLogRepository permissionLogRepository;

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Get user agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    /**
     * Get session ID from request
     */
    private String getSessionId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }

    /**
     * Get request ID from request header
     */
    private String getRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("X-Request-ID");
    }

    /**
     * Log group creation event
     */
    @Async
    public void logGroupCreated(Long groupId, Long userId, String groupName, HttpServletRequest request,
            Long responseTimeMs) {
        logGroupEvent("GROUP_CREATED", groupId, userId, null, null, groupName, null, null, null, null,
                request, responseTimeMs, true, null);
    }

    /**
     * Log group update event
     */
    @Async
    public void logGroupUpdated(Long groupId, Long userId, String groupName, HttpServletRequest request,
            Long responseTimeMs) {
        logGroupEvent("GROUP_UPDATED", groupId, userId, null, null, groupName, null, null, null, null,
                request, responseTimeMs, true, null);
    }

    /**
     * Log group deletion event
     */
    @Async
    public void logGroupDeleted(Long groupId, Long userId, String groupName, HttpServletRequest request,
            Long responseTimeMs) {
        logGroupEvent("GROUP_DELETED", groupId, userId, null, null, groupName, null, null, null, null,
                request, responseTimeMs, true, null);
    }

    /**
     * Log permission addition event
     */
    @Async
    public void logPermissionAdded(Long groupId, Long permissionId, Long userId, String permissionType,
            String resourceType, String resourceIdentifier, String action, HttpServletRequest request,
            Long responseTimeMs) {
        logPermissionEvent("PERMISSION_ADDED", groupId, permissionId, userId, null, null, permissionType,
                resourceType, resourceIdentifier, action, request, responseTimeMs, true, null);
    }

    /**
     * Log permission update event
     */
    @Async
    public void logPermissionUpdated(Long groupId, Long permissionId, Long userId, String permissionType,
            String resourceType, String resourceIdentifier, String action, HttpServletRequest request,
            Long responseTimeMs) {
        logPermissionEvent("PERMISSION_UPDATED", groupId, permissionId, userId, null, null, permissionType,
                resourceType, resourceIdentifier, action, request, responseTimeMs, true, null);
    }

    /**
     * Log permission removal event
     */
    @Async
    public void logPermissionRemoved(Long groupId, Long permissionId, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        logPermissionEvent("PERMISSION_REMOVED", groupId, permissionId, userId, null, null, null, null,
                null, null, request, responseTimeMs, true, null);
    }

    /**
     * Log user assignment to group event
     */
    @Async
    public void logUserAssigned(Long groupId, Long userId, Long targetUserId, String targetUserEmail,
            HttpServletRequest request, Long responseTimeMs) {
        logUserAssignmentEvent("USER_ASSIGNED", groupId, userId, targetUserId, targetUserEmail, request,
                responseTimeMs, true, null);
    }

    /**
     * Log user removal from group event
     */
    @Async
    public void logUserRemoved(Long groupId, Long userId, Long targetUserId, String targetUserEmail,
            HttpServletRequest request, Long responseTimeMs) {
        logUserAssignmentEvent("USER_REMOVED", groupId, userId, targetUserId, targetUserEmail, request,
                responseTimeMs, true, null);
    }

    /**
     * Log permission check event
     */
    @Async
    public void logPermissionChecked(Long userId, String permissionType, String resourceType,
            String resourceIdentifier, String action, boolean hasPermission, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            PermissionLog.EventDetails details = PermissionLog.EventDetails.builder()
                    .endpoint("/api/v1/permissions/check")
                    .method("POST")
                    .responseStatus(hasPermission ? 200 : 200) // Still 200 even if no permission
                    .permissionType(permissionType)
                    .resourceType(resourceType)
                    .resourceIdentifier(resourceIdentifier)
                    .action(action)
                    .additionalInfo(Map.of("hasPermission", hasPermission))
                    .build();

            PermissionLog permissionLog = PermissionLog.builder()
                    .eventType("PERMISSION_CHECKED")
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .responseTimeMs(responseTimeMs)
                    .details(details)
                    .build();

            permissionLogRepository.save(permissionLog);
            log.debug("Logged permission check for user ID: {}, hasPermission: {}", userId, hasPermission);
        } catch (Exception e) {
            log.error("Failed to log permission check event", e);
        }
    }

    /**
     * Helper method to log group events
     */
    @Async
    public void logGroupEvent(String eventType, Long groupId, Long userId, Long permissionId, Long targetUserId,
            String groupName, String permissionType, String resourceType, String resourceIdentifier, String action,
            HttpServletRequest request, Long responseTimeMs, boolean success, String failureReason) {
        try {
            String endpoint = "/api/v1/permissions/groups";
            String method = "POST";
            Integer responseStatus = 201;

            if ("GROUP_UPDATED".equals(eventType)) {
                endpoint = "/api/v1/permissions/groups/" + groupId;
                method = "PUT";
                responseStatus = 200;
            } else if ("GROUP_DELETED".equals(eventType)) {
                endpoint = "/api/v1/permissions/groups/" + groupId;
                method = "DELETE";
                responseStatus = 204;
            }

            PermissionLog.EventDetails details = PermissionLog.EventDetails.builder()
                    .endpoint(endpoint)
                    .method(method)
                    .responseStatus(responseStatus)
                    .groupName(groupName)
                    .build();

            PermissionLog permissionLog = PermissionLog.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .groupId(groupId)
                    .permissionId(permissionId)
                    .targetUserId(targetUserId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(success)
                    .failureReason(failureReason)
                    .responseTimeMs(responseTimeMs)
                    .details(details)
                    .build();

            permissionLogRepository.save(permissionLog);
            log.debug("Logged {} event for group ID: {}", eventType, groupId);
        } catch (Exception e) {
            log.error("Failed to log {} event", eventType, e);
        }
    }

    /**
     * Helper method to log permission events
     */
    @Async
    public void logPermissionEvent(String eventType, Long groupId, Long permissionId, Long userId,
            Long targetUserId, String groupName, String permissionType, String resourceType,
            String resourceIdentifier, String action, HttpServletRequest request, Long responseTimeMs,
            boolean success, String failureReason) {
        try {
            String endpoint = "/api/v1/permissions/groups/" + groupId + "/permissions";
            String method = "POST";
            Integer responseStatus = 201;

            if ("PERMISSION_UPDATED".equals(eventType)) {
                method = "PUT";
                responseStatus = 200;
            } else if ("PERMISSION_REMOVED".equals(eventType)) {
                method = "DELETE";
                responseStatus = 204;
            }

            PermissionLog.EventDetails details = PermissionLog.EventDetails.builder()
                    .endpoint(endpoint + (permissionId != null ? "/" + permissionId : ""))
                    .method(method)
                    .responseStatus(responseStatus)
                    .groupName(groupName)
                    .permissionType(permissionType)
                    .resourceType(resourceType)
                    .resourceIdentifier(resourceIdentifier)
                    .action(action)
                    .build();

            PermissionLog permissionLog = PermissionLog.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .groupId(groupId)
                    .permissionId(permissionId)
                    .targetUserId(targetUserId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(success)
                    .failureReason(failureReason)
                    .responseTimeMs(responseTimeMs)
                    .details(details)
                    .build();

            permissionLogRepository.save(permissionLog);
            log.debug("Logged {} event for permission ID: {}", eventType, permissionId);
        } catch (Exception e) {
            log.error("Failed to log {} event", eventType, e);
        }
    }

    /**
     * Helper method to log user assignment events
     */
    @Async
    public void logUserAssignmentEvent(String eventType, Long groupId, Long userId, Long targetUserId,
            String targetUserEmail, HttpServletRequest request, Long responseTimeMs, boolean success,
            String failureReason) {
        try {
            String endpoint = "/api/v1/permissions/groups/" + groupId + "/users";
            String method = "POST";
            Integer responseStatus = 200;

            if ("USER_REMOVED".equals(eventType)) {
                method = "DELETE";
                responseStatus = 204;
            }

            PermissionLog.EventDetails details = PermissionLog.EventDetails.builder()
                    .endpoint(endpoint)
                    .method(method)
                    .responseStatus(responseStatus)
                    .targetUserEmail(targetUserEmail)
                    .build();

            PermissionLog permissionLog = PermissionLog.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .groupId(groupId)
                    .targetUserId(targetUserId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(success)
                    .failureReason(failureReason)
                    .responseTimeMs(responseTimeMs)
                    .details(details)
                    .build();

            permissionLogRepository.save(permissionLog);
            log.debug("Logged {} event for group ID: {}, target user ID: {}", eventType, groupId, targetUserId);
        } catch (Exception e) {
            log.error("Failed to log {} event", eventType, e);
        }
    }

    /**
     * Log module creation event
     */
    @Async
    public void logModuleCreated(Long moduleId, Long userId, String moduleKey, String moduleName,
            HttpServletRequest request, Long responseTimeMs) {
        logModuleEvent("MODULE_CREATED", moduleId, userId, moduleKey, moduleName, request, responseTimeMs, true, null);
    }

    /**
     * Log module update event
     */
    @Async
    public void logModuleUpdated(Long moduleId, Long userId, String moduleKey, String moduleName,
            HttpServletRequest request, Long responseTimeMs) {
        logModuleEvent("MODULE_UPDATED", moduleId, userId, moduleKey, moduleName, request, responseTimeMs, true, null);
    }

    /**
     * Log module deletion event
     */
    @Async
    public void logModuleDeleted(Long moduleId, Long userId, String moduleKey, String moduleName,
            HttpServletRequest request, Long responseTimeMs) {
        logModuleEvent("MODULE_DELETED", moduleId, userId, moduleKey, moduleName, request, responseTimeMs, true, null);
    }

    /**
     * Helper method to log module events
     */
    @Async
    public void logModuleEvent(String eventType, Long moduleId, Long userId, String moduleKey, String moduleName,
            HttpServletRequest request, Long responseTimeMs, boolean success, String failureReason) {
        try {
            String endpoint = "/api/v1/permissions/modules";
            String method = "POST";
            Integer responseStatus = 201;

            if ("MODULE_UPDATED".equals(eventType)) {
                endpoint = "/api/v1/permissions/modules/" + moduleId;
                method = "PUT";
                responseStatus = 200;
            } else if ("MODULE_DELETED".equals(eventType)) {
                endpoint = "/api/v1/permissions/modules/" + moduleId;
                method = "DELETE";
                responseStatus = 204;
            }

            PermissionLog.EventDetails details = PermissionLog.EventDetails.builder()
                    .endpoint(endpoint)
                    .method(method)
                    .responseStatus(responseStatus)
                    .moduleKey(moduleKey)
                    .moduleName(moduleName)
                    .build();

            PermissionLog permissionLog = PermissionLog.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(success)
                    .failureReason(failureReason)
                    .responseTimeMs(responseTimeMs)
                    .details(details)
                    .build();

            permissionLogRepository.save(permissionLog);
            log.debug("Logged {} event for module ID: {}", eventType, moduleId);
        } catch (Exception e) {
            log.error("Failed to log {} event", eventType, e);
        }
    }

    /**
     * Log page creation event
     */
    @Async
    public void logPageCreated(Long pageId, Long userId, String moduleKey, String pageKey, String pageName,
            HttpServletRequest request, Long responseTimeMs) {
        logPageEvent("PAGE_CREATED", pageId, userId, moduleKey, pageKey, pageName, request, responseTimeMs, true,
                null);
    }

    /**
     * Log page update event
     */
    @Async
    public void logPageUpdated(Long pageId, Long userId, String moduleKey, String pageKey, String pageName,
            HttpServletRequest request, Long responseTimeMs) {
        logPageEvent("PAGE_UPDATED", pageId, userId, moduleKey, pageKey, pageName, request, responseTimeMs, true,
                null);
    }

    /**
     * Log page deletion event
     */
    @Async
    public void logPageDeleted(Long pageId, Long userId, String moduleKey, String pageKey, String pageName,
            HttpServletRequest request, Long responseTimeMs) {
        logPageEvent("PAGE_DELETED", pageId, userId, moduleKey, pageKey, pageName, request, responseTimeMs, true,
                null);
    }

    /**
     * Helper method to log page events
     */
    @Async
    public void logPageEvent(String eventType, Long pageId, Long userId, String moduleKey, String pageKey,
            String pageName, HttpServletRequest request, Long responseTimeMs, boolean success, String failureReason) {
        try {
            String endpoint = "/api/v1/permissions/modules/" + (moduleKey != null ? moduleKey : "") + "/pages";
            String method = "POST";
            Integer responseStatus = 201;

            if ("PAGE_UPDATED".equals(eventType)) {
                endpoint = "/api/v1/permissions/pages/" + pageId;
                method = "PUT";
                responseStatus = 200;
            } else if ("PAGE_DELETED".equals(eventType)) {
                endpoint = "/api/v1/permissions/pages/" + pageId;
                method = "DELETE";
                responseStatus = 204;
            }

            PermissionLog.EventDetails details = PermissionLog.EventDetails.builder()
                    .endpoint(endpoint)
                    .method(method)
                    .responseStatus(responseStatus)
                    .moduleKey(moduleKey)
                    .pageKey(pageKey)
                    .pageName(pageName)
                    .build();

            PermissionLog permissionLog = PermissionLog.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(success)
                    .failureReason(failureReason)
                    .responseTimeMs(responseTimeMs)
                    .details(details)
                    .build();

            permissionLogRepository.save(permissionLog);
            log.debug("Logged {} event for page ID: {}", eventType, pageId);
        } catch (Exception e) {
            log.error("Failed to log {} event", eventType, e);
        }
    }

    /**
     * Log component creation event
     */
    @Async
    public void logComponentCreated(Long componentId, Long userId, String pageKey, String componentKey,
            String componentName, HttpServletRequest request, Long responseTimeMs) {
        logComponentEvent("COMPONENT_CREATED", componentId, userId, pageKey, componentKey, componentName, request,
                responseTimeMs, true, null);
    }

    /**
     * Log component update event
     */
    @Async
    public void logComponentUpdated(Long componentId, Long userId, String pageKey, String componentKey,
            String componentName, HttpServletRequest request, Long responseTimeMs) {
        logComponentEvent("COMPONENT_UPDATED", componentId, userId, pageKey, componentKey, componentName, request,
                responseTimeMs, true, null);
    }

    /**
     * Log component deletion event
     */
    @Async
    public void logComponentDeleted(Long componentId, Long userId, String pageKey, String componentKey,
            String componentName, HttpServletRequest request, Long responseTimeMs) {
        logComponentEvent("COMPONENT_DELETED", componentId, userId, pageKey, componentKey, componentName, request,
                responseTimeMs, true, null);
    }

    /**
     * Helper method to log component events
     */
    @Async
    public void logComponentEvent(String eventType, Long componentId, Long userId, String pageKey,
            String componentKey, String componentName, HttpServletRequest request, Long responseTimeMs, boolean success,
            String failureReason) {
        try {
            String endpoint = "/api/v1/permissions/pages/" + (pageKey != null ? pageKey : "") + "/components";
            String method = "POST";
            Integer responseStatus = 201;

            if ("COMPONENT_UPDATED".equals(eventType)) {
                endpoint = "/api/v1/permissions/components/" + componentId;
                method = "PUT";
                responseStatus = 200;
            } else if ("COMPONENT_DELETED".equals(eventType)) {
                endpoint = "/api/v1/permissions/components/" + componentId;
                method = "DELETE";
                responseStatus = 204;
            }

            PermissionLog.EventDetails details = PermissionLog.EventDetails.builder()
                    .endpoint(endpoint)
                    .method(method)
                    .responseStatus(responseStatus)
                    .pageKey(pageKey)
                    .componentKey(componentKey)
                    .componentName(componentName)
                    .build();

            PermissionLog permissionLog = PermissionLog.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(success)
                    .failureReason(failureReason)
                    .responseTimeMs(responseTimeMs)
                    .details(details)
                    .build();

            permissionLogRepository.save(permissionLog);
            log.debug("Logged {} event for component ID: {}", eventType, componentId);
        } catch (Exception e) {
            log.error("Failed to log {} event", eventType, e);
        }
    }

    /**
     * Log registry cleanup event
     */
    @Async
    public void logRegistryCleanup(Long userId, Integer totalRemoved, Integer orphanedPagesRemoved,
            Integer orphanedComponentsRemoved, HttpServletRequest request, Long responseTimeMs) {
        try {
            PermissionLog.EventDetails details = PermissionLog.EventDetails.builder()
                    .endpoint("/api/v1/permissions/registry/cleanup")
                    .method("POST")
                    .responseStatus(200)
                    .additionalInfo(Map.of(
                            "totalRemoved", totalRemoved,
                            "orphanedPagesRemoved", orphanedPagesRemoved,
                            "orphanedComponentsRemoved", orphanedComponentsRemoved))
                    .build();

            PermissionLog permissionLog = PermissionLog.builder()
                    .eventType("REGISTRY_CLEANUP")
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .responseTimeMs(responseTimeMs)
                    .details(details)
                    .build();

            permissionLogRepository.save(permissionLog);
            log.debug("Logged registry cleanup event for user ID: {}, removed {} record(s)", userId, totalRemoved);
        } catch (Exception e) {
            log.error("Failed to log registry cleanup event", e);
        }
    }
}
