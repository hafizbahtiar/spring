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

import java.time.LocalDateTime;

/**
 * Payment method entity for storing saved payment methods.
 * Stripe-focused implementation for storing customer payment methods.
 */
@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_payment_methods_user_id", columnList = "user_id"),
        @Index(name = "idx_payment_methods_provider", columnList = "provider"),
        @Index(name = "idx_payment_methods_is_default", columnList = "is_default"),
        @Index(name = "idx_payment_methods_provider_method_id", columnList = "provider_method_id", unique = true),
        @Index(name = "idx_payment_methods_user_provider", columnList = "user_id, provider")
})
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this payment method
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Payment provider (STRIPE, PAYPAL, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private PaymentProvider provider;

    /**
     * Provider-specific payment method ID
     * For Stripe: PaymentMethod ID (pm_xxx)
     */
    @Column(name = "provider_method_id", length = 255, unique = true)
    private String providerMethodId;

    /**
     * Stripe-specific: Customer ID (cus_xxx)
     */
    @Column(name = "provider_customer_id", length = 255)
    private String providerCustomerId;

    /**
     * Payment method type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private PaymentMethodType type;

    /**
     * Last 4 digits of card (for cards)
     */
    @Column(name = "last4", length = 4)
    private String last4;

    /**
     * Card brand (e.g., visa, mastercard, amex)
     */
    @Column(name = "brand", length = 50)
    private String brand;

    /**
     * Card expiry month (1-12)
     */
    @Column(name = "expiry_month")
    private Integer expiryMonth;

    /**
     * Card expiry year (4 digits)
     */
    @Column(name = "expiry_year")
    private Integer expiryYear;

    /**
     * Whether this is the default payment method for the user
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * Provider-specific metadata (Stripe PaymentMethod metadata)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Object metadata;

    /**
     * Timestamp when payment method was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when payment method was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new payment method
     */
    public PaymentMethod(User user, PaymentProvider provider, PaymentMethodType type) {
        this.user = user;
        this.provider = provider;
        this.type = type;
        this.isDefault = false;
    }

    /**
     * Set this payment method as default
     * Note: Should unset other default methods for the same user
     */
    public void setAsDefault() {
        this.isDefault = true;
    }

    /**
     * Unset as default payment method
     */
    public void unsetAsDefault() {
        this.isDefault = false;
    }

    /**
     * Get formatted card display (e.g., "Visa •••• 4242")
     */
    public String getDisplayName() {
        if (type == PaymentMethodType.CREDIT_CARD || type == PaymentMethodType.DEBIT_CARD) {
            String brandDisplay = brand != null ? brand.substring(0, 1).toUpperCase() + brand.substring(1) : "Card";
            String last4Display = last4 != null ? last4 : "****";
            return String.format("%s •••• %s", brandDisplay, last4Display);
        }
        return type.getDisplayName();
    }
}
