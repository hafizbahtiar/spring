package com.hafizbahtiar.spring.features.subscription.repository;

import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.subscription.entity.Subscription;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlan;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Subscription entity.
 * Provides CRUD operations and custom queries for subscription management.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find all subscriptions for a specific user (paginated)
     */
    Page<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find subscriptions by user ID and status
     */
    List<Subscription> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SubscriptionStatus status);

    /**
     * Find user's active subscription
     */
    Optional<Subscription> findByUserIdAndStatusIn(Long userId, List<SubscriptionStatus> statuses);

    /**
     * Find subscription by Stripe Subscription ID (provider subscription ID)
     */
    Optional<Subscription> findByProviderSubscriptionId(String providerSubscriptionId);

    /**
     * Find subscriptions by provider customer ID
     */
    List<Subscription> findByProviderCustomerIdOrderByCreatedAtDesc(String providerCustomerId);

    /**
     * Find subscriptions by status
     */
    List<Subscription> findByStatusOrderByCreatedAtDesc(SubscriptionStatus status);

    /**
     * Find subscriptions by subscription plan
     */
    List<Subscription> findBySubscriptionPlanOrderByCreatedAtDesc(SubscriptionPlan subscriptionPlan);

    /**
     * Find active subscriptions by subscription plan
     */
    List<Subscription> findBySubscriptionPlanAndStatusIn(SubscriptionPlan subscriptionPlan,
            List<SubscriptionStatus> statuses);

    /**
     * Find subscriptions by provider
     */
    List<Subscription> findByProviderOrderByCreatedAtDesc(PaymentProvider provider);

    /**
     * Find subscriptions with upcoming renewals (for billing jobs)
     */
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate BETWEEN :startDate AND :endDate " +
            "AND s.status IN :activeStatuses ORDER BY s.nextBillingDate ASC")
    List<Subscription> findUpcomingRenewals(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("activeStatuses") List<SubscriptionStatus> activeStatuses);

    /**
     * Find expired subscriptions (past billing date, status is CANCELLED)
     */
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate < :now " +
            "AND s.status = :status ORDER BY s.nextBillingDate ASC")
    List<Subscription> findExpiredSubscriptions(
            @Param("now") LocalDateTime now,
            @Param("status") SubscriptionStatus status);

    /**
     * Find trial subscriptions (for trial expiration reminders)
     */
    @Query("SELECT s FROM Subscription s WHERE s.trialEnd BETWEEN :startDate AND :endDate " +
            "AND s.status = :status ORDER BY s.trialEnd ASC")
    List<Subscription> findTrialSubscriptionsExpiring(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") SubscriptionStatus status);

    /**
     * Count subscriptions by status for a user
     */
    long countByUserIdAndStatus(Long userId, SubscriptionStatus status);

    /**
     * Count active subscriptions for a plan
     */
    long countBySubscriptionPlanAndStatusIn(SubscriptionPlan subscriptionPlan, List<SubscriptionStatus> statuses);

    /**
     * Count subscriptions by provider
     */
    long countByProvider(PaymentProvider provider);

    /**
     * Find subscriptions within a date range
     */
    @Query("SELECT s FROM Subscription s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<Subscription> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find user subscriptions within a date range
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<Subscription> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
