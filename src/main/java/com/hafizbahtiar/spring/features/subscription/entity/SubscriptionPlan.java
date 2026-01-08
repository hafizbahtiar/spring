package com.hafizbahtiar.spring.features.subscription.entity;

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
 * Subscription plan entity for storing subscription plans.
 * Plans are DYNAMIC and ADMIN-MANAGEABLE (stored in database, not hardcoded).
 * Admin can create/edit/deactivate plans via API without code changes.
 */
@Entity
@Table(name = "subscription_plans", indexes = {
        @Index(name = "idx_subscription_plans_plan_type", columnList = "plan_type"),
        @Index(name = "idx_subscription_plans_is_active", columnList = "is_active"),
        @Index(name = "idx_subscription_plans_provider_plan_id", columnList = "provider_plan_id"),
        @Index(name = "idx_subscription_plans_billing_cycle", columnList = "billing_cycle"),
        @Index(name = "idx_subscription_plans_type_active", columnList = "plan_type, is_active")
})
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Plan name (e.g., "Basic Plan", "Pro Plan")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Plan description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Plan type (BASIC, PRO, ENTERPRISE, CUSTOM)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 50)
    private SubscriptionPlanType planType;

    /**
     * Plan price per billing cycle
     */
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * Currency code (ISO 4217, e.g., USD, EUR)
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    /**
     * Billing cycle (MONTHLY, QUARTERLY, YEARLY)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    /**
     * Plan features stored as JSON for flexibility
     * Example: {"maxUsers": 10, "maxStorage": "100GB", "apiCalls": 10000,
     * "supportLevel": "email"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    private Object features;

    /**
     * Whether this plan is active (available for new subscriptions)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Maximum users allowed (optional, can be in features JSON)
     */
    @Column(name = "max_users")
    private Integer maxUsers;

    /**
     * Maximum storage allowed (optional, can be in features JSON)
     */
    @Column(name = "max_storage", length = 50)
    private String maxStorage;

    /**
     * Stripe-specific: Stripe Price ID (maps to Stripe Price)
     * Used when creating Stripe Subscriptions
     */
    @Column(name = "provider_plan_id", length = 255)
    private String providerPlanId;

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
     * Timestamp when plan was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when plan was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new subscription plan
     */
    public SubscriptionPlan(String name, String description, SubscriptionPlanType planType,
            BigDecimal price, String currency, BillingCycle billingCycle) {
        this.name = name;
        this.description = description;
        this.planType = planType;
        this.price = price;
        this.currency = currency;
        this.billingCycle = billingCycle;
        this.isActive = true;
    }

    /**
     * Get display name for the plan
     */
    public String getDisplayName() {
        return name + " (" + billingCycle.getDisplayName() + ")";
    }

    /**
     * Get formatted price string
     */
    public String getFormattedPrice() {
        return currency + " " + price.toString();
    }

    /**
     * Deactivate this plan (soft delete)
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Activate this plan
     */
    public void activate() {
        this.isActive = true;
    }
}
