package com.hafizbahtiar.spring.features.payment.repository;

import com.hafizbahtiar.spring.features.payment.entity.Payment;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity.
 * Provides CRUD operations and custom queries for payment management.
 * Stripe-focused queries with extensibility for other providers.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find all payments for a specific user (paginated)
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find payments by user ID and status
     */
    List<Payment> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, PaymentStatus status);

    /**
     * Find payment by Stripe PaymentIntent ID (provider payment ID)
     */
    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    /**
     * Find payments by provider (Stripe, PayPal, etc.)
     */
    List<Payment> findByProviderOrderByCreatedAtDesc(PaymentProvider provider);

    /**
     * Find payments by status
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    /**
     * Find payments by subscription ID
     */
    List<Payment> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    /**
     * Find payments by order ID
     */
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * Find payments within a date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find user payments within a date range
     */
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdAndDateRange(@Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count payments by status for a user
     */
    long countByUserIdAndStatus(Long userId, PaymentStatus status);

    /**
     * Count payments by provider
     */
    long countByProvider(PaymentProvider provider);

    /**
     * Calculate total amount of completed payments for a user
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.id = :userId AND p.status = :status")
    BigDecimal sumAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);

    /**
     * Find recent payments for a user (last N payments)
     */
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find payments by Stripe customer ID (via PaymentMethod)
     */
    @Query("SELECT p FROM Payment p JOIN PaymentMethod pm ON p.paymentMethodId = pm.id WHERE pm.providerCustomerId = :customerId ORDER BY p.createdAt DESC")
    List<Payment> findByStripeCustomerId(@Param("customerId") String customerId);

    /**
     * Check if payment exists by provider payment ID
     */
    boolean existsByProviderPaymentId(String providerPaymentId);
}
