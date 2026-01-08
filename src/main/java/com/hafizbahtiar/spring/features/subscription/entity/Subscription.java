package com.hafizbahtiar.spring.features.subscription.entity;

import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Subscription entity for storing user subscriptions.
 * Represents a user's subscription to a subscription plan.
 */
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscriptions_user_id", columnList = "user_id"),
        @Index(name = "idx_subscriptions_status", columnList = "status"),
        @Index(name = "idx_subscriptions_provider_subscription_id", columnList = "provider_subscription_id", unique = true),
        @Index(name = "idx_subscriptions_subscription_plan_id", columnList = "subscription_plan_id"),
        @Index(name = "idx_subscriptions_next_billing_date", columnList = "next_billing_date"),
        @Index(name = "idx_subscriptions_user_status", columnList = "user_id, status"),
        @Index(name = "idx_subscriptions_provider_customer_id", columnList = "provider_customer_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this subscription
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Subscription plan this subscription is for
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    /**
     * Subscription status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SubscriptionStatus status = SubscriptionStatus.INCOMPLETE;

    /**
     * Payment provider (STRIPE, PAYPAL, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private PaymentProvider provider;

    /**
     * Provider-specific subscription ID (e.g., Stripe Subscription ID)
     */
    @Column(name = "provider_subscription_id", length = 255, unique = true)
    private String providerSubscriptionId;

    /**
     * Provider-specific customer ID (e.g., Stripe Customer ID)
     */
    @Column(name = "provider_customer_id", length = 255)
    private String providerCustomerId;

    /**
     * Start of current billing period
     */
    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    /**
     * End of current billing period
     */
    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    /**
     * Whether subscription will cancel at end of current period
     */
    @Column(name = "cancel_at_period_end", nullable = false)
    private Boolean cancelAtPeriodEnd = false;

    /**
     * Timestamp when subscription was cancelled
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Start of trial period (if applicable)
     */
    @Column(name = "trial_start")
    private LocalDateTime trialStart;

    /**
     * End of trial period (if applicable)
     */
    @Column(name = "trial_end")
    private LocalDateTime trialEnd;

    /**
     * Billing cycle (inherited from plan, but can be overridden)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    /**
     * Next billing date (when subscription will renew)
     */
    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    /**
     * Additional metadata stored as JSON
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
     * Timestamp when subscription was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when subscription was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new subscription
     */
    public Subscription(User user, SubscriptionPlan subscriptionPlan, PaymentProvider provider) {
        this.user = user;
        this.subscriptionPlan = subscriptionPlan;
        this.provider = provider;
        this.billingCycle = subscriptionPlan.getBillingCycle();
        this.status = SubscriptionStatus.INCOMPLETE;
        this.cancelAtPeriodEnd = false;
    }

    /**
     * Check if subscription is active (can use service)
     */
    public boolean isActive() {
        return status.isActive();
    }

    /**
     * Check if subscription is in trial period
     */
    public boolean isTrial() {
        return status == SubscriptionStatus.TRIALING && trialEnd != null && LocalDateTime.now().isBefore(trialEnd);
    }

    /**
     * Check if subscription can be cancelled
     */
    public boolean canBeCancelled() {
        return status.canBeCancelled();
    }

    /**
     * Cancel subscription (set to cancel at period end)
     */
    public void cancelAtPeriodEnd() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Subscription cannot be cancelled in status: " + status);
        }
        this.cancelAtPeriodEnd = true;
    }

    /**
     * Cancel subscription immediately
     */
    public void cancelImmediately() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Subscription cannot be cancelled in status: " + status);
        }
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelAtPeriodEnd = false;
    }

    /**
     * Reactivate cancelled subscription
     */
    public void reactivate() {
        if (status != SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Only cancelled subscriptions can be reactivated");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.cancelAtPeriodEnd = false;
        this.cancelledAt = null;
    }

    /**
     * Calculate days until next renewal
     */
    public Long daysUntilRenewal() {
        if (nextBillingDate == null) {
            return null;
        }
        return java.time.Duration.between(LocalDateTime.now(), nextBillingDate).toDays();
    }

    /**
     * Mark subscription as active
     */
    public void markAsActive() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    /**
     * Mark subscription as past due
     */
    public void markAsPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    /**
     * Mark subscription as expired
     */
    public void markAsExpired() {
        this.status = SubscriptionStatus.EXPIRED;
    }
}
