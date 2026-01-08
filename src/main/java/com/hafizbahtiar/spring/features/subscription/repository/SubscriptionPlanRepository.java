package com.hafizbahtiar.spring.features.subscription.repository;

import com.hafizbahtiar.spring.features.subscription.entity.BillingCycle;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlan;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SubscriptionPlan entity.
 * Provides CRUD operations and custom queries for subscription plan management.
 * Plans are dynamic and admin-manageable.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

        /**
         * Find all active plans (available for new subscriptions)
         */
        List<SubscriptionPlan> findByIsActiveTrueOrderByPlanTypeAscPriceAsc();

        /**
         * Find plans by plan type
         */
        List<SubscriptionPlan> findByPlanTypeOrderByPriceAsc(SubscriptionPlanType planType);

        /**
         * Find active plans by plan type
         */
        List<SubscriptionPlan> findByPlanTypeAndIsActiveTrueOrderByPriceAsc(SubscriptionPlanType planType);

        /**
         * Find plans by billing cycle
         */
        List<SubscriptionPlan> findByBillingCycleOrderByPriceAsc(BillingCycle billingCycle);

        /**
         * Find active plans by billing cycle
         */
        List<SubscriptionPlan> findByBillingCycleAndIsActiveTrueOrderByPriceAsc(BillingCycle billingCycle);

        /**
         * Find plan by Stripe Price ID (provider plan ID)
         */
        Optional<SubscriptionPlan> findByProviderPlanId(String providerPlanId);

        /**
         * Find plans by plan type and billing cycle
         */
        List<SubscriptionPlan> findByPlanTypeAndBillingCycleAndIsActiveTrueOrderByPriceAsc(
                        SubscriptionPlanType planType, BillingCycle billingCycle);

        /**
         * Check if plan exists by provider plan ID
         */
        boolean existsByProviderPlanId(String providerPlanId);

        /**
         * Count active plans
         */
        long countByIsActiveTrue();

        /**
         * Count plans by plan type
         */
        long countByPlanType(SubscriptionPlanType planType);

        /**
         * Find plans with active subscriptions count
         * Used to prevent deletion of plans in use
         */
        @Query("SELECT sp FROM SubscriptionPlan sp LEFT JOIN Subscription s ON s.subscriptionPlan.id = sp.id " +
                        "WHERE sp.id = :planId GROUP BY sp.id HAVING COUNT(s.id) = 0")
        Optional<SubscriptionPlan> findByIdWithNoActiveSubscriptions(@Param("planId") Long planId);

        /**
         * Count active subscriptions for a given plan ID.
         * Active subscriptions include: ACTIVE, TRIALING, PAST_DUE, INCOMPLETE, UNPAID.
         * Used to prevent deactivation of plans that are currently in use.
         */
        @Query("SELECT COUNT(s) FROM Subscription s WHERE s.subscriptionPlan.id = :planId " +
                        "AND s.status IN ('ACTIVE', 'TRIALING', 'PAST_DUE', 'INCOMPLETE', 'UNPAID')")
        long countActiveSubscriptionsByPlanId(@Param("planId") Long planId);
}
