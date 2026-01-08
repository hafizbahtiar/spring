package com.hafizbahtiar.spring.features.payment.repository;

import com.hafizbahtiar.spring.features.payment.entity.PaymentMethod;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.entity.PaymentMethodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentMethod entity.
 * Provides CRUD operations and custom queries for payment method management.
 * Stripe-focused queries.
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Find all payment methods for a user
     */
    List<PaymentMethod> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find payment methods by user and provider (Stripe, PayPal, etc.)
     */
    List<PaymentMethod> findByUserIdAndProviderOrderByCreatedAtDesc(Long userId, PaymentProvider provider);

    /**
     * Find default payment method for a user
     */
    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Find default payment method for a user and provider
     */
    Optional<PaymentMethod> findByUserIdAndProviderAndIsDefaultTrue(Long userId, PaymentProvider provider);

    /**
     * Find payment method by Stripe PaymentMethod ID
     */
    Optional<PaymentMethod> findByProviderMethodId(String providerMethodId);

    /**
     * Find payment methods by Stripe customer ID
     */
    List<PaymentMethod> findByProviderCustomerIdOrderByCreatedAtDesc(String providerCustomerId);

    /**
     * Find payment methods by type (e.g., CREDIT_CARD)
     */
    List<PaymentMethod> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, PaymentMethodType type);

    /**
     * Count payment methods for a user
     */
    long countByUserId(Long userId);

    /**
     * Count payment methods by provider for a user
     */
    long countByUserIdAndProvider(Long userId, PaymentProvider provider);

    /**
     * Unset all default payment methods for a user
     * Used when setting a new default payment method
     */
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.user.id = :userId")
    void unsetAllDefaultsByUserId(@Param("userId") Long userId);

    /**
     * Unset all default payment methods for a user and provider
     */
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.user.id = :userId AND pm.provider = :provider")
    void unsetAllDefaultsByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") PaymentProvider provider);

    /**
     * Check if payment method exists by provider method ID
     */
    boolean existsByProviderMethodId(String providerMethodId);
}
