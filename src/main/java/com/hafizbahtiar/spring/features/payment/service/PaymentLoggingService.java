package com.hafizbahtiar.spring.features.payment.service;

import com.hafizbahtiar.spring.features.payment.model.PaymentLog;
import com.hafizbahtiar.spring.features.payment.repository.mongodb.PaymentLogRepository;
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
 * Service for logging payment events to MongoDB.
 * Provides methods to log various payment-related events for audit and
 * analytics purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLoggingService {

    private final PaymentLogRepository paymentLogRepository;

    /**
     * Log payment creation event
     */
    @Async
    public void logPaymentCreated(Long paymentId, Long userId, String provider, String providerPaymentId,
            BigDecimal amount, String currency, String status, HttpServletRequest request, Long responseTimeMs) {
        try {
            PaymentLog.PaymentEventDetails details = PaymentLog.PaymentEventDetails.builder()
                    .endpoint("/api/v1/payments")
                    .method("POST")
                    .responseStatus(201)
                    .responseTimeMs(responseTimeMs)
                    .build();

            PaymentLog paymentLog = PaymentLog.builder()
                    .paymentId(paymentId)
                    .userId(userId)
                    .eventType("PAYMENT_CREATED")
                    .provider(provider)
                    .providerPaymentId(providerPaymentId)
                    .amount(amount)
                    .currency(currency)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            paymentLogRepository.save(paymentLog);
            log.debug("Logged payment creation for paymentId: {}", paymentId);
        } catch (Exception e) {
            log.error("Failed to log payment creation event", e);
            // Don't throw exception - logging failure shouldn't break payment flow
        }
    }

    /**
     * Log payment confirmation event
     */
    @Async
    public void logPaymentConfirmed(Long paymentId, Long userId, String provider, String providerPaymentId,
            String status, HttpServletRequest request, Long responseTimeMs) {
        try {
            PaymentLog.PaymentEventDetails details = PaymentLog.PaymentEventDetails.builder()
                    .endpoint("/api/v1/payments/" + paymentId + "/confirm")
                    .method("POST")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .build();

            PaymentLog paymentLog = PaymentLog.builder()
                    .paymentId(paymentId)
                    .userId(userId)
                    .eventType("PAYMENT_CONFIRMED")
                    .provider(provider)
                    .providerPaymentId(providerPaymentId)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            paymentLogRepository.save(paymentLog);
            log.debug("Logged payment confirmation for paymentId: {}", paymentId);
        } catch (Exception e) {
            log.error("Failed to log payment confirmation event", e);
        }
    }

    /**
     * Log payment failure event
     */
    @Async
    public void logPaymentFailed(Long paymentId, Long userId, String provider, String providerPaymentId,
            String failureReason, String failureCode, HttpServletRequest request) {
        try {
            PaymentLog paymentLog = PaymentLog.builder()
                    .paymentId(paymentId)
                    .userId(userId)
                    .eventType("PAYMENT_FAILED")
                    .provider(provider)
                    .providerPaymentId(providerPaymentId)
                    .status("FAILED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(false)
                    .failureReason(failureReason)
                    .failureCode(failureCode)
                    .build();

            paymentLogRepository.save(paymentLog);
            log.debug("Logged payment failure for paymentId: {}", paymentId);
        } catch (Exception e) {
            log.error("Failed to log payment failure event", e);
        }
    }

    /**
     * Log refund event
     */
    @Async
    public void logRefund(Long paymentId, Long userId, String provider, String providerPaymentId,
            String providerRefundId, BigDecimal refundAmount, HttpServletRequest request, Long responseTimeMs) {
        try {
            PaymentLog.PaymentEventDetails details = PaymentLog.PaymentEventDetails.builder()
                    .endpoint("/api/v1/payments/" + paymentId + "/refund")
                    .method("POST")
                    .responseStatus(200)
                    .responseTimeMs(responseTimeMs)
                    .refundAmount(refundAmount)
                    .providerRefundId(providerRefundId)
                    .build();

            PaymentLog paymentLog = PaymentLog.builder()
                    .paymentId(paymentId)
                    .userId(userId)
                    .eventType("PAYMENT_REFUNDED")
                    .provider(provider)
                    .providerPaymentId(providerPaymentId)
                    .status("REFUNDED")
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            paymentLogRepository.save(paymentLog);
            log.debug("Logged refund for paymentId: {}", paymentId);
        } catch (Exception e) {
            log.error("Failed to log refund event", e);
        }
    }

    /**
     * Log payment method added event
     */
    @Async
    public void logPaymentMethodAdded(Long paymentMethodId, Long userId, String provider,
            String providerMethodId, HttpServletRequest request) {
        try {
            PaymentLog.PaymentEventDetails details = PaymentLog.PaymentEventDetails.builder()
                    .endpoint("/api/v1/payments/methods")
                    .method("POST")
                    .responseStatus(201)
                    .paymentMethodId(paymentMethodId)
                    .build();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("providerMethodId", providerMethodId);

            PaymentLog paymentLog = PaymentLog.builder()
                    .userId(userId)
                    .eventType("PAYMENT_METHOD_ADDED")
                    .provider(provider)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .metadata(metadata)
                    .build();

            paymentLogRepository.save(paymentLog);
            log.debug("Logged payment method addition for paymentMethodId: {}", paymentMethodId);
        } catch (Exception e) {
            log.error("Failed to log payment method addition event", e);
        }
    }

    /**
     * Log payment method removed event
     */
    @Async
    public void logPaymentMethodRemoved(Long paymentMethodId, Long userId, String provider,
            HttpServletRequest request) {
        try {
            PaymentLog.PaymentEventDetails details = PaymentLog.PaymentEventDetails.builder()
                    .endpoint("/api/v1/payments/methods/" + paymentMethodId)
                    .method("DELETE")
                    .responseStatus(204)
                    .paymentMethodId(paymentMethodId)
                    .build();

            PaymentLog paymentLog = PaymentLog.builder()
                    .userId(userId)
                    .eventType("PAYMENT_METHOD_REMOVED")
                    .provider(provider)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .details(details)
                    .build();

            paymentLogRepository.save(paymentLog);
            log.debug("Logged payment method removal for paymentMethodId: {}", paymentMethodId);
        } catch (Exception e) {
            log.error("Failed to log payment method removal event", e);
        }
    }

    /**
     * Log webhook received event
     */
    @Async
    public void logWebhookReceived(String provider, String providerPaymentId, String eventType,
            String payload, HttpServletRequest request) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("webhookPayload", payload);
            metadata.put("webhookEventType", eventType);

            PaymentLog paymentLog = PaymentLog.builder()
                    .eventType("WEBHOOK_RECEIVED")
                    .provider(provider)
                    .providerPaymentId(providerPaymentId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .metadata(metadata)
                    .build();

            paymentLogRepository.save(paymentLog);
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
