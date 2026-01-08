package com.hafizbahtiar.spring.features.payment.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity for storing payment transactions.
 * Focused on Stripe integration, extensible for PayPal.
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_user_id", columnList = "user_id"),
        @Index(name = "idx_payments_provider", columnList = "provider"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_provider_payment_id", columnList = "provider_payment_id", unique = true),
        @Index(name = "idx_payments_created_at", columnList = "created_at"),
        @Index(name = "idx_payments_user_status", columnList = "user_id, status")
})
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who made the payment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Order ID (nullable for standalone payments/subscriptions)
     */
    @Column(name = "order_id")
    private Long orderId;

    /**
     * Subscription ID (nullable, for SaaS subscriptions)
     */
    @Column(name = "subscription_id")
    private Long subscriptionId;

    /**
     * Payment provider (STRIPE, PAYPAL, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private PaymentProvider provider;

    /**
     * Provider-specific payment ID (e.g., Stripe PaymentIntent ID)
     */
    @Column(name = "provider_payment_id", length = 255, unique = true)
    private String providerPaymentId;

    /**
     * Payment amount
     */
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Currency code (ISO 4217, e.g., USD, EUR)
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    /**
     * Payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Payment method ID (reference to PaymentMethod entity)
     */
    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    /**
     * Payment method type (for quick reference)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type", length = 50)
    private PaymentMethodType paymentMethodType;

    /**
     * Stripe-specific: PaymentIntent client secret (for frontend confirmation)
     */
    @Column(name = "client_secret", length = 255)
    private String clientSecret;

    /**
     * Failure reason if payment failed
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * Stripe-specific: Failure code (e.g., card_declined, insufficient_funds)
     */
    @Column(name = "failure_code", length = 100)
    private String failureCode;

    /**
     * Refund amount (if partially refunded)
     */
    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    /**
     * Timestamp when payment was refunded
     */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /**
     * Stripe-specific: PaymentIntent metadata
     * Stores additional provider-specific data as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Object metadata;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Timestamp when payment was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when payment was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new payment
     */
    public Payment(User user, PaymentProvider provider, BigDecimal amount, String currency) {
        this.user = user;
        this.provider = provider;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
    }

    /**
     * Mark payment as completed
     */
    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
    }

    /**
     * Mark payment as failed
     */
    public void markAsFailed(String reason, String code) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failureCode = code;
    }

    /**
     * Mark payment as refunded
     */
    public void markAsRefunded(BigDecimal refundAmount) {
        this.status = PaymentStatus.REFUNDED;
        this.refundAmount = refundAmount;
        this.refundedAt = LocalDateTime.now();
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED;
    }

    /**
     * Check if payment is in a terminal state
     */
    public boolean isTerminal() {
        return status.isTerminal();
    }
}
