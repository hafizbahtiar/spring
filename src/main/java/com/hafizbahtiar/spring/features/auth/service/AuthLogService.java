package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.dto.AuthLogResponse;

import java.util.List;

/**
 * Service interface for retrieving authentication logs.
 * Provides methods to query authentication event logs for users and admins.
 */
public interface AuthLogService {

    /**
     * Get authentication logs for a specific user.
     * Users can only access their own logs.
     * 
     * @param userId User ID
     * @param limit  Maximum number of logs to return
     * @return List of authentication logs
     */
    List<AuthLogResponse> getUserAuthLogs(Long userId, int limit);

    /**
     * Get security events (failed logins, token invalidations, etc.).
     * Only accessible by OWNER and ADMIN roles.
     * 
     * @param limit Maximum number of logs to return
     * @return List of security event logs
     */
    List<AuthLogResponse> getSecurityLogs(int limit);

    /**
     * Get failed login attempts for a specific identifier (email/username).
     * Only accessible by OWNER and ADMIN roles.
     * 
     * @param identifier Email or username
     * @param limit      Maximum number of logs to return
     * @return List of failed login attempt logs
     */
    List<AuthLogResponse> getFailedLoginAttempts(String identifier, int limit);
}
