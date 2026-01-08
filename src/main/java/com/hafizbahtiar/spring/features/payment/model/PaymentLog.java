package com.hafizbahtiar.spring.features.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MongoDB document for payment event logging.
 * Stores payment-related events like payment creation, confirmation, refunds,
 * and webhook events for audit and analytics purposes.
 */
@Document(collection = "payment_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLog {

    @Id
    private String id;

    /**
     * Payment ID (from PostgreSQL)
     */
    @Indexed
    private Long paymentId;

    /**
     * User ID who made the payment
     */
    @Indexed
    private Long userId;

    /**
     * Type of payment event
     * Values: PAYMENT_CREATED, PAYMENT_CONFIRMED, PAYMENT_FAILED, PAYMENT_REFUNDED,
     * PAYMENT_CANCELLED, WEBHOOK_RECEIVED, PAYMENT_METHOD_ADDED,
     * PAYMENT_METHOD_REMOVED
     */
    @Indexed
    private String eventType;

    /**
     * Payment provider (STRIPE, PAYPAL, etc.)
     */
    @Indexed
    private String provider;

    /**
     * Provider-specific payment ID (e.g., Stripe PaymentIntent ID)
     */
    @Indexed
    private String providerPaymentId;

    /**
     * Payment amount
     */
    private BigDecimal amount;

    /**
     * Currency code (ISO 4217)
     */
    private String currency;

    /**
     * Payment status
     */
    private String status;

    /**
     * Timestamp when the event occurred
     */
    @Indexed
    private LocalDateTime timestamp;

    /**
     * IP address of the client (for API calls)
     */
    private String ipAddress;

    /**
     * User agent string from the request
     */
    private String userAgent;

    /**
     * Session ID (if available)
     */
    @Indexed
    private String sessionId;

    /**
     * Request ID for tracing
     */
    @Indexed
    private String requestId;

    /**
     * Success status of the operation
     */
    private Boolean success;

    /**
     * Failure reason (if success is false)
     */
    private String failureReason;

    /**
     * Failure code (provider-specific, e.g., Stripe error codes)
     */
    private String failureCode;

    /**
     * Response time in milliseconds (for performance tracking)
     */
    private Long responseTimeMs;

    /**
     * Event details (endpoint, method, refund amount, etc.)
     */
    private PaymentEventDetails details;

    /**
     * Additional metadata as flexible JSON structure
     */
    private Object metadata;

    /**
     * Inner class for payment event details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentEventDetails {
        /**
         * API endpoint accessed
         */
        private String endpoint;

        /**
         * HTTP method (GET, POST, PUT, DELETE, etc.)
         */
        private String method;

        /**
         * HTTP response status code
         */
        private Integer responseStatus;

        /**
         * Response time in milliseconds
         */
        private Long responseTimeMs;

        /**
         * Refund amount (for refund events)
         */
        private BigDecimal refundAmount;

        /**
         * Provider refund ID (for refund events)
         */
        private String providerRefundId;

        /**
         * Payment method ID (if applicable)
         */
        private Long paymentMethodId;

        /**
         * Order ID (if applicable)
         */
        private Long orderId;

        /**
         * Subscription ID (if applicable)
         */
        private Long subscriptionId;

        /**
         * Additional context-specific data
         */
        private Object additionalData;
    }
}
