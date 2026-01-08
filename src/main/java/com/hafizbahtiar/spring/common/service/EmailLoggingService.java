package com.hafizbahtiar.spring.common.service;

import com.hafizbahtiar.spring.common.model.EmailLog;
import com.hafizbahtiar.spring.common.repository.mongodb.EmailLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Service for logging email events to MongoDB.
 * Provides async logging for email sending attempts (success and failure).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailLoggingService {

    private final EmailLogRepository emailLogRepository;

    /**
     * Log a successful email sending event.
     *
     * @param to             Recipient email address
     * @param subject        Email subject
     * @param templateName   Template name (if template-based, null otherwise)
     * @param emailType      Email type (PLAIN_TEXT, HTML, TEMPLATED)
     * @param userId         User ID associated with the email (if applicable)
     * @param responseTimeMs Response time in milliseconds
     * @param metadata       Additional metadata (optional)
     */
    @Async
    public void logEmailSent(String to, String subject, String templateName, String emailType,
            Long userId, Long responseTimeMs, Object metadata) {
        try {
            HttpServletRequest request = getCurrentRequest();
            EmailLog emailLog = EmailLog.builder()
                    .to(to)
                    .subject(subject)
                    .templateName(templateName)
                    .emailType(emailType)
                    .sentAt(LocalDateTime.now())
                    .status("SENT")
                    .userId(userId)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .responseTimeMs(responseTimeMs)
                    .metadata(metadata)
                    .build();
            emailLogRepository.save(emailLog);
            log.debug("Logged successful email sent to: {}, template: {}", to, templateName);
        } catch (Exception e) {
            log.error("Failed to log email sent event", e);
        }
    }

    /**
     * Log a failed email sending event.
     *
     * @param to           Recipient email address
     * @param subject      Email subject
     * @param templateName Template name (if template-based, null otherwise)
     * @param emailType    Email type (PLAIN_TEXT, HTML, TEMPLATED)
     * @param userId       User ID associated with the email (if applicable)
     * @param errorMessage Error message describing the failure
     * @param metadata     Additional metadata (optional)
     */
    @Async
    public void logEmailFailed(String to, String subject, String templateName, String emailType,
            Long userId, String errorMessage, Object metadata) {
        try {
            HttpServletRequest request = getCurrentRequest();
            EmailLog emailLog = EmailLog.builder()
                    .to(to)
                    .subject(subject)
                    .templateName(templateName)
                    .emailType(emailType)
                    .sentAt(LocalDateTime.now())
                    .status("FAILED")
                    .errorMessage(errorMessage)
                    .userId(userId)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .metadata(metadata)
                    .build();
            emailLogRepository.save(emailLog);
            log.debug("Logged failed email attempt to: {}, error: {}", to, errorMessage);
        } catch (Exception e) {
            log.error("Failed to log email failed event", e);
        }
    }

    /**
     * Get current HTTP request from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Extract client IP address from request
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
     * Extract user agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    /**
     * Extract request ID from request (if available)
     */
    private String getRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // Check common request ID headers
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = request.getHeader("X-Correlation-ID");
        }
        return requestId;
    }
}
