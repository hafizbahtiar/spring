package com.hafizbahtiar.spring.features.subscription.model;

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
 * MongoDB document for subscription event logging.
 * Stores subscription-related events like subscription creation, cancellation,
 * updates,
 * and webhook events for audit and analytics purposes.
 */
@Document(collection = "subscription_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionLog {

    @Id
    private String id;

    /**
     * Subscription ID (from PostgreSQL)
     */
    @Indexed
    private Long subscriptionId;

    /**
     * User ID who owns the subscription
     */
    @Indexed
    private Long userId;

    /**
     * Subscription plan ID
     */
    @Indexed
    private Long subscriptionPlanId;

    /**
     * Type of subscription event
     * Values: SUBSCRIPTION_CREATED, SUBSCRIPTION_CANCELLED, SUBSCRIPTION_UPDATED,
     * SUBSCRIPTION_REACTIVATED, SUBSCRIPTION_RENEWED, SUBSCRIPTION_EXPIRED,
     * PLAN_CHANGED, PAYMENT_SUCCESS, PAYMENT_FAILED, TRIAL_STARTED, TRIAL_ENDED,
     * WEBHOOK_RECEIVED
     */
    @Indexed
    private String eventType;

    /**
     * Payment provider (STRIPE, PAYPAL, etc.)
     */
    @Indexed
    private String provider;

    /**
     * Provider-specific subscription ID (e.g., Stripe Subscription ID)
     */
    @Indexed
    private String providerSubscriptionId;

    /**
     * Subscription status
     */
    private String status;

    /**
     * Subscription plan name
     */
    private String planName;

    /**
     * Subscription amount (if applicable)
     */
    private BigDecimal amount;

    /**
     * Currency code (ISO 4217)
     */
    private String currency;

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
     * Event details (endpoint, method, plan changes, etc.)
     */
    private SubscriptionEventDetails details;

    /**
     * Additional metadata as flexible JSON structure
     */
    private Object metadata;

    /**
     * Inner class for subscription event details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionEventDetails {
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
         * Previous plan ID (for plan changes)
         */
        private Long previousPlanId;

        /**
         * New plan ID (for plan changes)
         */
        private Long newPlanId;

        /**
         * Previous status (for status changes)
         */
        private String previousStatus;

        /**
         * New status (for status changes)
         */
        private String newStatus;

        /**
         * Cancel immediately flag (for cancellation events)
         */
        private Boolean cancelImmediately;

        /**
         * Cancel at period end flag
         */
        private Boolean cancelAtPeriodEnd;

        /**
         * Trial days (for trial events)
         */
        private Integer trialDays;

        /**
         * Trial start date
         */
        private LocalDateTime trialStart;

        /**
         * Trial end date
         */
        private LocalDateTime trialEnd;

        /**
         * Payment ID (for payment-related events)
         */
        private Long paymentId;

        /**
         * Provider payment ID (for payment-related events)
         */
        private String providerPaymentId;

        /**
         * Additional context-specific data
         */
        private Object additionalData;
    }
}
