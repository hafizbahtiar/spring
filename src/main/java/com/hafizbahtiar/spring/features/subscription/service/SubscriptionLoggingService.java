package com.hafizbahtiar.spring.features.subscription.service;

import com.hafizbahtiar.spring.features.subscription.model.SubscriptionLog;
import com.hafizbahtiar.spring.features.subscription.repository.mongodb.SubscriptionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for logging subscription events to MongoDB.
 * Provides methods to log various subscription-related events for audit and
 * analytics purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionLoggingService {

    private final SubscriptionLogRepository subscriptionLogRepository;

    /**
     * Log subscription creation event
     */
    @Async
    public void logSubscriptionCreated(Long subscriptionId, Long userId, Long subscriptionPlanId,
            String provider, String providerSubscriptionId, String planName, String status,
            Integer trialDays, LocalDateTime trialStart, LocalDateTime trialEnd,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            SubscriptionLog.SubscriptionEventDetails details = SubscriptionLog.SubscriptionEventDetails.builder()
                    .endpoint("/api/v1/subscriptions")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .trialDays(trialDays)
                    .trialStart(trialStart)
                    .trialEnd(trialEnd)
                    .build();

            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscriptionId(subscriptionId)
                    .userId(userId)
                    .subscriptionPlanId(subscriptionPlanId)
                    .eventType("SUBSCRIPTION_CREATED")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .planName(planName)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged subscription creation for subscriptionId: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to log subscription creation event", e);
            // Don't throw exception - logging failure shouldn't break subscription flow
        }
    }

    /**
     * Log subscription cancellation event
     */
    @Async
    public void logSubscriptionCancelled(Long subscriptionId, Long userId, Long subscriptionPlanId,
            String provider, String providerSubscriptionId, String planName, String status,
            boolean cancelImmediately, HttpServletRequest request, Long responseTimeMs) {
        try {
            SubscriptionLog.SubscriptionEventDetails details = SubscriptionLog.SubscriptionEventDetails.builder()
                    .endpoint("/api/v1/subscriptions/" + subscriptionId + "/cancel")
                    .method("POST")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .cancelImmediately(cancelImmediately)
                    .cancelAtPeriodEnd(!cancelImmediately)
                    .previousStatus(status)
                    .newStatus("CANCELLED")
                    .build();

            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscriptionId(subscriptionId)
                    .userId(userId)
                    .subscriptionPlanId(subscriptionPlanId)
                    .eventType("SUBSCRIPTION_CANCELLED")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .planName(planName)
                    .status("CANCELLED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged subscription cancellation for subscriptionId: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to log subscription cancellation event", e);
        }
    }

    /**
     * Log subscription update event
     */
    @Async
    public void logSubscriptionUpdated(Long subscriptionId, Long userId, Long subscriptionPlanId,
            String provider, String providerSubscriptionId, String planName, String status,
            Long previousPlanId, Long newPlanId, String previousStatus, String newStatus,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            SubscriptionLog.SubscriptionEventDetails details = SubscriptionLog.SubscriptionEventDetails.builder()
                    .endpoint("/api/v1/subscriptions/" + subscriptionId)
                    .method("PUT")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .previousPlanId(previousPlanId)
                    .newPlanId(newPlanId)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .build();

            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscriptionId(subscriptionId)
                    .userId(userId)
                    .subscriptionPlanId(subscriptionPlanId)
                    .eventType("SUBSCRIPTION_UPDATED")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .planName(planName)
                    .status(newStatus)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged subscription update for subscriptionId: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to log subscription update event", e);
        }
    }

    /**
     * Log subscription reactivation event
     */
    @Async
    public void logSubscriptionReactivated(Long subscriptionId, Long userId, Long subscriptionPlanId,
            String provider, String providerSubscriptionId, String planName, String status,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            SubscriptionLog.SubscriptionEventDetails details = SubscriptionLog.SubscriptionEventDetails.builder()
                    .endpoint("/api/v1/subscriptions/" + subscriptionId + "/reactivate")
                    .method("POST")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .previousStatus("CANCELLED")
                    .newStatus(status)
                    .build();

            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscriptionId(subscriptionId)
                    .userId(userId)
                    .subscriptionPlanId(subscriptionPlanId)
                    .eventType("SUBSCRIPTION_REACTIVATED")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .planName(planName)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged subscription reactivation for subscriptionId: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to log subscription reactivation event", e);
        }
    }

    /**
     * Log subscription renewal event
     */
    @Async
    public void logSubscriptionRenewed(Long subscriptionId, Long userId, Long subscriptionPlanId,
            String provider, String providerSubscriptionId, String planName, String status,
            Long paymentId, String providerPaymentId, BigDecimal amount, String currency,
            HttpServletRequest request, Long responseTimeMs) {
        try {
            SubscriptionLog.SubscriptionEventDetails details = SubscriptionLog.SubscriptionEventDetails.builder()
                    .endpoint("/api/v1/subscriptions/" + subscriptionId + "/renew")
                    .method("POST")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .paymentId(paymentId)
                    .providerPaymentId(providerPaymentId)
                    .build();

            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscriptionId(subscriptionId)
                    .userId(userId)
                    .subscriptionPlanId(subscriptionPlanId)
                    .eventType("SUBSCRIPTION_RENEWED")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .planName(planName)
                    .status(status)
                    .amount(amount)
                    .currency(currency)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged subscription renewal for subscriptionId: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to log subscription renewal event", e);
        }
    }

    /**
     * Log subscription payment success event
     */
    @Async
    public void logPaymentSuccess(Long subscriptionId, Long userId, Long subscriptionPlanId,
            String provider, String providerSubscriptionId, String planName, String status,
            Long paymentId, String providerPaymentId, BigDecimal amount, String currency,
            HttpServletRequest request) {
        try {
            SubscriptionLog.SubscriptionEventDetails details = SubscriptionLog.SubscriptionEventDetails.builder()
                    .paymentId(paymentId)
                    .providerPaymentId(providerPaymentId)
                    .build();

            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscriptionId(subscriptionId)
                    .userId(userId)
                    .subscriptionPlanId(subscriptionPlanId)
                    .eventType("PAYMENT_SUCCESS")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .planName(planName)
                    .status(status)
                    .amount(amount)
                    .currency(currency)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged payment success for subscriptionId: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to log payment success event", e);
        }
    }

    /**
     * Log subscription payment failure event
     */
    @Async
    public void logPaymentFailure(Long subscriptionId, Long userId, Long subscriptionPlanId,
            String provider, String providerSubscriptionId, String planName, String status,
            String failureReason, String failureCode, HttpServletRequest request) {
        try {
            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscriptionId(subscriptionId)
                    .userId(userId)
                    .subscriptionPlanId(subscriptionPlanId)
                    .eventType("PAYMENT_FAILED")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .planName(planName)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(false)
                    .failureReason(failureReason)
                    .failureCode(failureCode)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged payment failure for subscriptionId: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to log payment failure event", e);
        }
    }

    /**
     * Log subscription status update event (from webhook)
     */
    @Async
    public void logStatusUpdate(Long subscriptionId, Long userId, Long subscriptionPlanId,
            String provider, String providerSubscriptionId, String planName,
            String previousStatus, String newStatus, HttpServletRequest request) {
        try {
            SubscriptionLog.SubscriptionEventDetails details = SubscriptionLog.SubscriptionEventDetails.builder()
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .build();

            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .subscriptionId(subscriptionId)
                    .userId(userId)
                    .subscriptionPlanId(subscriptionPlanId)
                    .eventType("SUBSCRIPTION_STATUS_UPDATED")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .planName(planName)
                    .status(newStatus)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged status update for subscriptionId: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to log status update event", e);
        }
    }

    /**
     * Log webhook received event
     */
    @Async
    public void logWebhookReceived(String provider, String providerSubscriptionId, String eventType,
            String payload, HttpServletRequest request) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("webhookPayload", payload);
            metadata.put("webhookEventType", eventType);

            SubscriptionLog subscriptionLog = SubscriptionLog.builder()
                    .eventType("WEBHOOK_RECEIVED")
                    .provider(provider)
                    .providerSubscriptionId(providerSubscriptionId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .metadata(metadata)
                    .build();

            subscriptionLogRepository.save(subscriptionLog);
            log.debug("Logged webhook received for provider: {}, eventType: {}", provider, eventType);
        } catch (Exception e) {
            log.error("Failed to log webhook received event", e);
        }
    }

    // Helper methods for extracting request information

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private String getSessionId(HttpServletRequest request) {
        return request != null && request.getSession(false) != null
                ? request.getSession(false).getId()
                : null;
    }

    private String getRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = request.getHeader("X-Correlation-ID");
        }
        return requestId;
    }
}
