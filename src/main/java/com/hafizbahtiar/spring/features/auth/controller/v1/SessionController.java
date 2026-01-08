package com.hafizbahtiar.spring.features.auth.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.auth.dto.SessionResponse;
import com.hafizbahtiar.spring.features.auth.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for session management endpoints.
 * Handles session viewing and revocation operations.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Get current session ID from request header or generate from JWT
     * For now, we'll use a simple approach - in production, you might want to
     * include sessionId in JWT claims or use a custom header
     */
    private String getCurrentSessionId(HttpServletRequest request) {
        // Try to get from custom header first
        String sessionId = request != null ? request.getHeader("X-Session-ID") : null;
        if (sessionId != null && !sessionId.isEmpty()) {
            return sessionId;
        }
        // Fallback: Use a hash of user agent + IP (not ideal, but works for now)
        // In production, sessionId should be included in JWT or stored in token
        // blacklist
        if (request != null) {
            String userAgent = request.getHeader("User-Agent");
            String ip = getClientIpAddress(request);
            if (userAgent != null && ip != null) {
                return String.valueOf((userAgent + ip).hashCode());
            }
        }
        return null;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Get all active sessions for current user
     * GET /api/v1/sessions
     * Requires: Authenticated user
     * Returns: List of active sessions
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getActiveSessions(HttpServletRequest request) {
        Long userId = getCurrentUserId();
        log.info("Active sessions fetch request received for user ID: {}", userId);
        List<SessionResponse> sessions = sessionService.getActiveSessions(userId);
        return ResponseUtils.ok(sessions);
    }

    /**
     * Revoke a specific session
     * DELETE /api/v1/sessions/{sessionId}
     * Requires: Authenticated user
     * Returns: No content
     */
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @PathVariable String sessionId,
            HttpServletRequest request) {
        Long userId = getCurrentUserId();
        log.info("Session revocation request received for sessionId: {}, user ID: {}", sessionId, userId);
        sessionService.revokeSession(userId, sessionId);
        return ResponseUtils.noContent();
    }

    /**
     * Revoke all sessions except current
     * DELETE /api/v1/sessions
     * Requires: Authenticated user
     * Returns: No content
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessions(HttpServletRequest request) {
        Long userId = getCurrentUserId();
        String currentSessionId = getCurrentSessionId(request);
        log.info("Revoke all sessions request received for user ID: {}, currentSessionId: {}", userId,
                currentSessionId);
        sessionService.revokeAllSessions(userId, currentSessionId);
        return ResponseUtils.noContent();
    }
}
