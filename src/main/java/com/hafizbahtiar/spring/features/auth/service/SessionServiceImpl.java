package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.dto.SessionResponse;
import com.hafizbahtiar.spring.features.auth.entity.Session;
import com.hafizbahtiar.spring.features.auth.exception.SessionNotFoundException;
import com.hafizbahtiar.spring.features.auth.mapper.SessionMapper;
import com.hafizbahtiar.spring.features.auth.repository.SessionRepository;
import com.hafizbahtiar.spring.features.ipaddress.service.IPGeolocationService;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of SessionService.
 * Handles session creation, validation, and revocation with device/location
 * tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final UserRepository userRepository;
    private final AuthLoggingService authLoggingService;
    private final IPGeolocationService ipGeolocationService;

    // User agent parsing patterns
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "(?i)(mobile|android|iphone|ipod|blackberry|opera mini|windows phone|palm|iemobile)");
    private static final Pattern TABLET_PATTERN = Pattern.compile(
            "(?i)(tablet|ipad|playbook|silk|kindle)");
    private static final Pattern CHROME_PATTERN = Pattern.compile("(?i)chrome/([\\d.]+)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("(?i)firefox/([\\d.]+)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("(?i)version/([\\d.]+).*safari");
    private static final Pattern EDGE_PATTERN = Pattern.compile("(?i)edg[ea]?/([\\d.]+)");
    private static final Pattern OPERA_PATTERN = Pattern.compile("(?i)opr/([\\d.]+)|opera/([\\d.]+)");
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("(?i)windows nt ([\\d.]+)");
    private static final Pattern MACOS_PATTERN = Pattern.compile("(?i)mac os x ([\\d_]+)");
    private static final Pattern LINUX_PATTERN = Pattern.compile("(?i)linux");
    private static final Pattern IOS_PATTERN = Pattern.compile("(?i)os ([\\d_]+) like mac os x");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("(?i)android ([\\d.]+)");

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions(Long userId) {
        log.debug("Fetching active sessions for user ID: {}", userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        List<Session> sessions = sessionRepository.findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(userId);
        return sessionMapper.toResponseList(sessions);
    }

    @Override
    public void revokeSession(Long userId, String sessionId) {
        log.debug("Revoking session {} for user ID: {}", sessionId, userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Find and validate session belongs to user
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> SessionNotFoundException.bySessionId(sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new SecurityException("Session does not belong to user");
        }

        // Revoke session
        session.revoke();
        sessionRepository.save(session);

        log.info("Session {} revoked for user ID: {}", sessionId, userId);

        // Log session revocation
        authLoggingService.logSessionRevoked(
                userId,
                sessionId,
                getCurrentRequest());
    }

    @Override
    public void revokeAllSessions(Long userId, String currentSessionId) {
        log.debug("Revoking all sessions except {} for user ID: {}", currentSessionId, userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Revoke all sessions except current
        sessionRepository.revokeAllSessionsExceptCurrent(userId, currentSessionId);

        log.info("All sessions revoked except {} for user ID: {}", currentSessionId, userId);

        // Log all sessions revoked
        authLoggingService.logAllSessionsRevoked(
                userId,
                currentSessionId,
                getCurrentRequest());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SessionResponse createSession(Long userId, HttpServletRequest request) {
        log.debug("Creating session for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Extract request information
        String userAgent = getUserAgent(request);
        String ipAddress = getClientIpAddress(request);

        // Create session
        Session session = new Session(user, userAgent, ipAddress);

        // Parse user agent to extract device info
        parseUserAgent(userAgent, session);

        // Save session first (don't block on geolocation)
        Session savedSession = sessionRepository.save(session);
        log.info("Session created for user ID: {}, sessionId: {}", userId, savedSession.getSessionId());

        // Enrich session with IP geolocation data asynchronously (non-blocking)
        ipGeolocationService.getGeolocationAsync(ipAddress)
                .thenAccept(geoData -> {
                    if (geoData != null) {
                        try {
                            // Update session with geolocation data
                            savedSession.setCountry(geoData.getCountryCode());
                            savedSession.setRegion(geoData.getRegion());
                            savedSession.setCity(geoData.getCity());
                            savedSession.setLatitude(geoData.getLatitude());
                            savedSession.setLongitude(geoData.getLongitude());
                            savedSession.setTimezone(geoData.getTimezone());
                            savedSession.setIsp(geoData.getIsp());

                            // Save updated session
                            sessionRepository.save(savedSession);

                            log.debug("Updated session {} with geolocation data: {} ({}, {})",
                                    savedSession.getSessionId(),
                                    geoData.getCity(),
                                    geoData.getRegion(),
                                    geoData.getCountryCode());
                        } catch (Exception e) {
                            log.warn("Failed to update session {} with geolocation data: {}",
                                    savedSession.getSessionId(), e.getMessage());
                        }
                    } else {
                        log.debug("No geolocation data available for IP: {}", ipAddress);
                    }
                })
                .exceptionally(ex -> {
                    log.warn("Failed to enrich session {} with geolocation for IP {}: {}",
                            savedSession.getSessionId(), ipAddress, ex.getMessage());
                    return null;
                });

        // Log session creation
        authLoggingService.logSessionCreated(
                userId,
                savedSession.getSessionId(),
                getCurrentRequest());

        return sessionMapper.toResponse(savedSession);
    }

    @Override
    public void updateSessionActivity(String sessionId) {
        sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .ifPresent(session -> {
                    session.updateActivity();
                    sessionRepository.save(session);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionActive(String sessionId) {
        return sessionRepository.findBySessionIdAndIsActiveTrue(sessionId).isPresent();
    }

    /**
     * Parse user agent string to extract device, browser, and OS information
     */
    private void parseUserAgent(String userAgent, Session session) {
        if (userAgent == null || userAgent.isEmpty()) {
            return;
        }

        // Detect device type
        if (TABLET_PATTERN.matcher(userAgent).find()) {
            session.setDeviceType("tablet");
        } else if (MOBILE_PATTERN.matcher(userAgent).find()) {
            session.setDeviceType("mobile");
        } else {
            session.setDeviceType("desktop");
        }

        // Detect browser
        if (CHROME_PATTERN.matcher(userAgent).find()) {
            session.setBrowser("Chrome");
        } else if (FIREFOX_PATTERN.matcher(userAgent).find()) {
            session.setBrowser("Firefox");
        } else if (SAFARI_PATTERN.matcher(userAgent).find()) {
            session.setBrowser("Safari");
        } else if (EDGE_PATTERN.matcher(userAgent).find()) {
            session.setBrowser("Edge");
        } else if (OPERA_PATTERN.matcher(userAgent).find()) {
            session.setBrowser("Opera");
        } else {
            session.setBrowser("Unknown");
        }

        // Detect OS
        if (IOS_PATTERN.matcher(userAgent).find()) {
            session.setOs("iOS");
        } else if (ANDROID_PATTERN.matcher(userAgent).find()) {
            session.setOs("Android");
        } else if (MACOS_PATTERN.matcher(userAgent).find()) {
            session.setOs("macOS");
        } else if (WINDOWS_PATTERN.matcher(userAgent).find()) {
            session.setOs("Windows");
        } else if (LINUX_PATTERN.matcher(userAgent).find()) {
            session.setOs("Linux");
        } else {
            session.setOs("Unknown");
        }

        // Generate device name (simplified)
        String deviceName = generateDeviceName(session.getDeviceType(), session.getBrowser(), session.getOs());
        session.setDeviceName(deviceName);
    }

    /**
     * Generate a simple device name from device type, browser, and OS
     */
    private String generateDeviceName(String deviceType, String browser, String os) {
        if (deviceType == null || os == null) {
            return "Unknown Device";
        }

        if ("mobile".equals(deviceType) || "tablet".equals(deviceType)) {
            return String.format("%s on %s", os, deviceType);
        } else {
            return String.format("%s on %s", browser, os);
        }
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
        // Handle comma-separated IPs (from proxies)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Get user agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : "unknown";
    }

    /**
     * Get current HTTP request from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attributes = (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("Could not get current request: {}", e.getMessage());
            return null;
        }
    }
}
