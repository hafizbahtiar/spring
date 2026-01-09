package com.hafizbahtiar.spring.security;

import com.hafizbahtiar.spring.common.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final com.hafizbahtiar.spring.features.auth.service.SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Extract sessionId from token (if present)
                String sessionId = null;
                try {
                    sessionId = tokenProvider.getSessionIdFromToken(jwt);
                } catch (Exception e) {
                    log.debug("No sessionId in token or failed to extract: {}", e.getMessage());
                }

                // Validate session if sessionId is present
                if (sessionId != null && !sessionService.isSessionActive(sessionId)) {
                    log.warn("Session {} is not active, rejecting authentication", sessionId);
                    // Don't set authentication - request will be treated as unauthenticated
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = tokenProvider.getUsernameFromToken(jwt);
                Long userId = tokenProvider.getUserIdFromToken(jwt);
                UUID userUuid = tokenProvider.getUserUuidFromToken(jwt);
                String role = tokenProvider.getRoleFromToken(jwt);

                // Create UserPrincipal with user details
                UserPrincipal userPrincipal = UserPrincipal.create(userId, userUuid, username, username, role);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Update session activity if sessionId is present
                if (sessionId != null) {
                    try {
                        sessionService.updateSessionActivity(sessionId);
                    } catch (Exception e) {
                        log.debug("Failed to update session activity for sessionId: {}", sessionId, e);
                        // Don't fail authentication if session update fails
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
