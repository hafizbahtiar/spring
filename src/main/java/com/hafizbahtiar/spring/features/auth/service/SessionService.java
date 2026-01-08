package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.dto.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service interface for session management.
 * Handles session creation, validation, and revocation.
 */
public interface SessionService {

    /**
     * Get all active sessions for a user.
     *
     * @param userId User ID
     * @return List of active sessions
     */
    List<SessionResponse> getActiveSessions(Long userId);

    /**
     * Revoke a specific session.
     *
     * @param userId    User ID (for authorization check)
     * @param sessionId Session ID to revoke
     */
    void revokeSession(Long userId, String sessionId);

    /**
     * Revoke all sessions for a user except the current one.
     *
     * @param userId           User ID
     * @param currentSessionId Current session ID to keep active
     */
    void revokeAllSessions(Long userId, String currentSessionId);

    /**
     * Create a new session for a user (called on login).
     *
     * @param userId  User ID
     * @param request HTTP request to extract device/location info
     * @return Created session response
     */
    SessionResponse createSession(Long userId, HttpServletRequest request);

    /**
     * Update session activity timestamp (called on each authenticated request).
     *
     * @param sessionId Session ID
     */
    void updateSessionActivity(String sessionId);

    /**
     * Validate if a session is active.
     *
     * @param sessionId Session ID
     * @return true if session is active, false otherwise
     */
    boolean isSessionActive(String sessionId);
}
