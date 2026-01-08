package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.entity.Session;
import com.hafizbahtiar.spring.features.auth.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for cleaning up expired sessions.
 * Runs scheduled tasks to automatically revoke expired refresh tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupService {

    private final SessionRepository sessionRepository;

    /**
     * Clean up expired refresh tokens.
     * Runs daily at 2:00 AM to revoke sessions with expired refresh tokens.
     * 
     * Cron expression: "0 0 2 * * ?"
     * - Second: 0
     * - Minute: 0
     * - Hour: 2 (2 AM)
     * - Day of month: * (every day)
     * - Month: * (every month)
     * - Day of week: ? (no specific day)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredSessions() {
        log.info("Starting scheduled cleanup of expired refresh tokens...");

        LocalDateTime now = LocalDateTime.now();

        try {
            // Find all sessions with expired refresh tokens
            List<Session> expiredSessions = sessionRepository.findByRefreshTokenExpiresAtBefore(now);

            if (expiredSessions.isEmpty()) {
                log.info("No expired sessions found. Cleanup completed.");
                return;
            }

            int totalExpired = expiredSessions.size();
            int revokedCount = 0;
            int alreadyInactiveCount = 0;

            // Revoke expired sessions
            for (Session session : expiredSessions) {
                if (Boolean.TRUE.equals(session.getIsActive())) {
                    session.revoke();
                    sessionRepository.save(session);
                    revokedCount++;
                } else {
                    alreadyInactiveCount++;
                }
            }

            log.info("Session cleanup completed successfully:");
            log.info("  - Total expired sessions found: {}", totalExpired);
            log.info("  - Sessions revoked: {}", revokedCount);
            log.info("  - Sessions already inactive: {}", alreadyInactiveCount);

        } catch (Exception e) {
            log.error("Error during session cleanup", e);
        }
    }

    /**
     * Manual cleanup method for testing or administrative purposes.
     * Can be called programmatically if needed.
     * 
     * @return Cleanup statistics
     */
    @Transactional
    public CleanupStats manualCleanup() {
        log.info("Manual cleanup of expired refresh tokens initiated...");

        LocalDateTime now = LocalDateTime.now();

        try {
            List<Session> expiredSessions = sessionRepository.findByRefreshTokenExpiresAtBefore(now);

            if (expiredSessions.isEmpty()) {
                log.info("No expired sessions found.");
                return CleanupStats.builder()
                        .totalExpired(0)
                        .revoked(0)
                        .alreadyInactive(0)
                        .build();
            }

            int totalExpired = expiredSessions.size();
            int revokedCount = 0;
            int alreadyInactiveCount = 0;

            for (Session session : expiredSessions) {
                if (Boolean.TRUE.equals(session.getIsActive())) {
                    session.revoke();
                    sessionRepository.save(session);
                    revokedCount++;
                } else {
                    alreadyInactiveCount++;
                }
            }

            CleanupStats stats = CleanupStats.builder()
                    .totalExpired(totalExpired)
                    .revoked(revokedCount)
                    .alreadyInactive(alreadyInactiveCount)
                    .build();

            log.info("Manual cleanup completed: {}", stats);
            return stats;

        } catch (Exception e) {
            log.error("Error during manual session cleanup", e);
            throw new RuntimeException("Failed to cleanup expired sessions", e);
        }
    }

    /**
     * Statistics for session cleanup operations.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CleanupStats {
        private int totalExpired;
        private int revoked;
        private int alreadyInactive;
    }
}
