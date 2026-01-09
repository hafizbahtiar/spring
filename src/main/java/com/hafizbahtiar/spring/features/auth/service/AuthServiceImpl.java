package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.exception.InvalidCredentialsException;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.auth.dto.LoginRequest;
import com.hafizbahtiar.spring.features.auth.dto.LoginResponse;
import com.hafizbahtiar.spring.features.auth.dto.TokenRefreshResponse;
import com.hafizbahtiar.spring.features.auth.entity.Session;
import com.hafizbahtiar.spring.features.auth.repository.SessionRepository;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.mapper.UserMapper;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import com.hafizbahtiar.spring.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthLoggingService authLoggingService;
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for identifier: {}", request.getIdentifier());
        HttpServletRequest httpRequest = getCurrentRequest();

        try {
            // Find user by email or username
            User user = userRepository.findByEmailOrUsernameAndActive(request.getIdentifier())
                    .orElseThrow(() -> {
                        // Log failed login attempt - user not found
                        authLoggingService.logLoginFailure(
                                request.getIdentifier(),
                                "USER_NOT_FOUND",
                                httpRequest);
                        return InvalidCredentialsException.defaultMessage();
                    });

            // Validate password
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.warn("Invalid password attempt for user: {}", user.getEmail());
                // Log failed login attempt - password mismatch
                authLoggingService.logLoginFailure(
                        request.getIdentifier(),
                        "PASSWORD_MISMATCH",
                        httpRequest);
                throw InvalidCredentialsException.passwordMismatch();
            }

            // Update last login
            user.updateLastLogin();
            userRepository.save(user);

            // Create session for this login (before generating token so we can include
            // sessionId)
            String sessionId = null;
            LocalDateTime refreshTokenExpiresAt = null;
            try {
                com.hafizbahtiar.spring.features.auth.dto.SessionResponse sessionResponse = sessionService
                        .createSession(user.getId(), httpRequest);
                sessionId = sessionResponse.getId(); // sessionId is the UUID (used as refresh token)
                refreshTokenExpiresAt = sessionResponse.getRefreshTokenExpiresAt(); // 7 days from creation
                log.debug("Session created for user: {}, sessionId: {}, refreshTokenExpiresAt: {}", 
                        user.getId(), sessionId, refreshTokenExpiresAt);
            } catch (Exception e) {
                log.error("Failed to create session for user: {}", user.getId(), e);
                // Don't fail login if session creation fails - session can be created later
            }

            // Generate JWT token with sessionId included
            String token = jwtTokenProvider.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getUuid(),
                    user.getRole(),
                    sessionId);

            // Calculate expiration time
            Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
            LocalDateTime expiresAt = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            // Map user to response
            UserResponse userResponse = userMapper.toResponse(user);

            log.info("User logged in successfully: {}", user.getEmail());

            // Log successful login
            authLoggingService.logLoginSuccess(
                    user.getId(),
                    user.getEmail(),
                    sessionId,
                    expiresAt,
                    httpRequest);

            return LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .expiresAt(expiresAt)
                    .refreshToken(sessionId) // sessionId is used as refresh token
                    .refreshTokenExpiresAt(refreshTokenExpiresAt) // 7 days from session creation
                    .user(userResponse)
                    .build();
        } catch (InvalidCredentialsException e) {
            // Re-throw after logging
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw InvalidCredentialsException.defaultMessage();
        }

        // Get UserPrincipal from authentication
        if (authentication
                .getPrincipal() instanceof com.hafizbahtiar.spring.common.security.UserPrincipal userPrincipal) {
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> UserNotFoundException.byId(userPrincipal.getId()));
            return userMapper.toResponse(user);
        }

        // Fallback to username lookup (backward compatibility)
        String username = authentication.getName();
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> UserNotFoundException.byEmail(username));

        return userMapper.toResponse(user);
    }

    @Override
    public boolean validateToken(String token) {
        HttpServletRequest httpRequest = getCurrentRequest();
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Log token validation attempt
        if (isValid) {
            // Extract user info from token for logging
            try {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                authLoggingService.logTokenValidation(userId, username, true, httpRequest);
            } catch (Exception e) {
                log.debug("Could not extract user info from token for logging", e);
            }
        } else {
            authLoggingService.logTokenValidation(null, null, false, httpRequest);
        }

        return isValid;
    }

    @Override
    public void logout() {
        HttpServletRequest httpRequest = getCurrentRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Logout attempted but user is not authenticated");
            return;
        }

        // Get user ID from authentication
        Long userId = null;
        String identifier = null;
        String sessionId = null;

        if (authentication
                .getPrincipal() instanceof com.hafizbahtiar.spring.common.security.UserPrincipal userPrincipal) {
            userId = userPrincipal.getId();
            identifier = userPrincipal.getEmail();
        } else {
            log.warn("Could not extract user principal for logout");
            return;
        }

        // Try to extract sessionId from JWT token if available
        try {
            String jwt = getJwtFromRequest(httpRequest);
            if (jwt != null) {
                sessionId = jwtTokenProvider.getSessionIdFromToken(jwt);
            }
        } catch (Exception e) {
            log.debug("Could not extract sessionId from token: {}", e.getMessage());
        }

        // Revoke session if sessionId is available
        if (sessionId != null) {
            try {
                sessionService.revokeSession(userId, sessionId);
                log.info("Session {} revoked for user ID: {}", sessionId, userId);
            } catch (Exception e) {
                log.error("Failed to revoke session {} for user ID: {}", sessionId, userId, e);
                // Continue with logout even if session revocation fails
            }
        }

        // Log logout event
        authLoggingService.logLogout(userId, identifier, sessionId, httpRequest);

        // Clear authentication from security context
        SecurityContextHolder.clearContext();
        log.info("User {} logged out successfully", identifier);
    }

    @Override
    public TokenRefreshResponse refreshToken(String refreshToken, HttpServletRequest request) {
        log.debug("Refresh token request received");
        HttpServletRequest httpRequest = request != null ? request : getCurrentRequest();

        try {
            // Find session by sessionId (refreshToken) with active status and valid expiration
            LocalDateTime now = LocalDateTime.now();
            Session session = sessionRepository
                    .findBySessionIdAndIsActiveTrueAndRefreshTokenExpiresAtAfter(refreshToken, now)
                    .orElseThrow(() -> {
                        // Log failed refresh attempt
                        authLoggingService.logTokenRefreshFailure(
                                refreshToken,
                                "SESSION_NOT_FOUND_OR_INACTIVE_OR_EXPIRED",
                                httpRequest);
                        return InvalidCredentialsException.defaultMessage();
                    });

            // Validate user is still active
            User user = session.getUser();
            if (user == null || !user.isActive()) {
                log.warn("Refresh token attempt for inactive user: sessionId={}", refreshToken);
                authLoggingService.logTokenRefreshFailure(
                        refreshToken,
                        "USER_INACTIVE",
                        httpRequest);
                throw InvalidCredentialsException.defaultMessage();
            }

            // Update session activity
            session.updateActivity();
            sessionRepository.save(session);

            // Generate new JWT access token (15 minutes, includes sessionId)
            String newToken = jwtTokenProvider.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getUuid(),
                    user.getRole(),
                    session.getSessionId());

            // Calculate expiration time
            Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(newToken);
            LocalDateTime expiresAt = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            log.info("Token refreshed successfully for user: {}, sessionId: {}", user.getEmail(), refreshToken);

            // Log successful token refresh
            authLoggingService.logTokenRefreshSuccess(
                    user.getId(),
                    user.getEmail(),
                    refreshToken,
                    expiresAt,
                    httpRequest);

            return TokenRefreshResponse.builder()
                    .token(newToken)
                    .type("Bearer")
                    .expiresAt(expiresAt)
                    // Note: Not returning refreshToken/refreshTokenExpiresAt (no token rotation for now)
                    .build();
        } catch (InvalidCredentialsException e) {
            // Re-throw after logging
            throw e;
        }
    }

    /**
     * Get JWT token from request
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Get current HTTP request from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
