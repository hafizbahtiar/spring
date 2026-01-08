package com.hafizbahtiar.spring.features.subscription.service;

import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionPlanRequest;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionPlanResponse;
import com.hafizbahtiar.spring.features.subscription.entity.BillingCycle;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlanType;

import java.util.List;

/**
 * Service interface for subscription plan management.
 * Plans are dynamic and admin-manageable (stored in database, not hardcoded).
 */
public interface SubscriptionPlanService {

    /**
     * Create a new subscription plan (admin only).
     * 
     * @param request Plan creation request
     * @return Created SubscriptionPlanResponse
     */
    SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request);

    /**
     * Update an existing subscription plan (admin only).
     * 
     * @param planId  Plan ID
     * @param request Plan update request
     * @return Updated SubscriptionPlanResponse
     */
    SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanRequest request);

    /**
     * Get subscription plan by ID.
     * 
     * @param planId Plan ID
     * @return SubscriptionPlanResponse
     */
    SubscriptionPlanResponse getPlan(Long planId);

    /**
     * List all active plans (public endpoint).
     * 
     * @return List of active SubscriptionPlanResponse
     */
    List<SubscriptionPlanResponse> listActivePlans();

    /**
     * List plans by plan type.
     * 
     * @param planType Plan type filter
     * @return List of SubscriptionPlanResponse
     */
    List<SubscriptionPlanResponse> listPlansByType(SubscriptionPlanType planType);

    /**
     * List plans by billing cycle.
     * 
     * @param billingCycle Billing cycle filter
     * @return List of SubscriptionPlanResponse
     */
    List<SubscriptionPlanResponse> listPlansByBillingCycle(BillingCycle billingCycle);

    /**
     * Deactivate a subscription plan (admin only).
     * Soft delete - sets isActive = false, doesn't delete from database.
     * 
     * @param planId Plan ID
     */
    void deactivatePlan(Long planId);

    /**
     * Activate a subscription plan (admin only).
     * 
     * @param planId Plan ID
     */
    void activatePlan(Long planId);
}
