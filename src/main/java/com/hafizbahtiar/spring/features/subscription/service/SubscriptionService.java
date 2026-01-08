package com.hafizbahtiar.spring.features.subscription.service;

import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionRequest;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionResponse;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionStatusResponse;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionUpdateRequest;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for subscription management.
 * Handles subscription lifecycle, payments, and status updates.
 */
public interface SubscriptionService {

    /**
     * Create a new subscription with payment method.
     * 
     * @param userId  User ID
     * @param request Subscription creation request
     * @return Created SubscriptionResponse
     */
    SubscriptionResponse createSubscription(Long userId, SubscriptionRequest request);

    /**
     * Cancel a subscription (immediate or at period end).
     * 
     * @param subscriptionId    Subscription ID
     * @param cancelImmediately Whether to cancel immediately or at period end
     * @return Updated SubscriptionResponse
     */
    SubscriptionResponse cancelSubscription(Long subscriptionId, boolean cancelImmediately);

    /**
     * Update subscription (plan change, cancel at period end setting).
     * 
     * @param subscriptionId Subscription ID
     * @param request        Update request
     * @return Updated SubscriptionResponse
     */
    SubscriptionResponse updateSubscription(Long subscriptionId, SubscriptionUpdateRequest request);

    /**
     * Process subscription renewal payment.
     * Called automatically by billing job or webhook.
     * 
     * @param subscriptionId Subscription ID
     * @return Updated SubscriptionResponse
     */
    SubscriptionResponse renewSubscription(Long subscriptionId);

    /**
     * Get subscription by ID.
     * 
     * @param subscriptionId Subscription ID
     * @return SubscriptionResponse
     */
    SubscriptionResponse getSubscription(Long subscriptionId);

    /**
     * Get subscription by provider subscription ID (for webhook processing).
     * 
     * @param providerSubscriptionId Provider-specific subscription ID
     * @return SubscriptionResponse
     */
    SubscriptionResponse getSubscriptionByProviderId(String providerSubscriptionId);

    /**
     * List user subscriptions (paginated).
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of SubscriptionResponse
     */
    Page<SubscriptionResponse> getUserSubscriptions(Long userId, Pageable pageable);

    /**
     * Get user's active subscription.
     * 
     * @param userId User ID
     * @return SubscriptionResponse or null if no active subscription
     */
    SubscriptionResponse getActiveSubscription(Long userId);

    /**
     * Update subscription status from webhook (internal use).
     * 
     * @param providerSubscriptionId Provider-specific subscription ID
     * @param status                 New subscription status
     */
    void updateSubscriptionStatus(String providerSubscriptionId, SubscriptionStatus status);

    /**
     * Handle successful subscription payment (from webhook or payment service).
     * 
     * @param subscriptionId Subscription ID
     * @param paymentId      Payment ID
     */
    void handlePaymentSuccess(Long subscriptionId, Long paymentId);

    /**
     * Handle failed subscription payment (from webhook or payment service).
     * 
     * @param subscriptionId Subscription ID
     * @param reason         Failure reason
     */
    void handlePaymentFailure(Long subscriptionId, String reason);

    /**
     * Get subscription status.
     * 
     * @param subscriptionId Subscription ID
     * @return SubscriptionStatusResponse
     */
    SubscriptionStatusResponse getSubscriptionStatus(Long subscriptionId);

    /**
     * Reactivate a cancelled subscription.
     * 
     * @param subscriptionId Subscription ID
     * @return Updated SubscriptionResponse
     */
    SubscriptionResponse reactivateSubscription(Long subscriptionId);
}
