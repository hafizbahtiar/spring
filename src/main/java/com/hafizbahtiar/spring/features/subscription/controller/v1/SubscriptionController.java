package com.hafizbahtiar.spring.features.subscription.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionRequest;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionResponse;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionStatusResponse;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionUpdateRequest;
import com.hafizbahtiar.spring.features.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for subscription management endpoints.
 * Handles subscription creation, cancellation, updates, and status checks.
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SecurityService securityService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Create a new subscription
     * POST /api/v1/subscriptions
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody SubscriptionRequest request) {
        Long userId = getCurrentUserId();
        log.info("Subscription creation request received for user ID: {}, plan ID: {}", userId, request.getPlanId());
        SubscriptionResponse response = subscriptionService.createSubscription(userId, request);
        return ResponseUtils.created(response, "Subscription created successfully");
    }

    /**
     * Get subscription by ID
     * GET /api/v1/subscriptions/{id}
     * Requires: User owns the subscription OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(@PathVariable Long id) {
        log.debug("Fetching subscription with ID: {}", id);

        SubscriptionResponse subscription = subscriptionService.getSubscription(id);
        Long userId = getCurrentUserId();
        if (!subscription.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own subscriptions");
        }

        return ResponseUtils.ok(subscription);
    }

    /**
     * List subscriptions for current user (paginated)
     * GET /api/v1/subscriptions
     * Requires: Authenticated user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginatedResponse<SubscriptionResponse>>> listSubscriptions(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Listing subscriptions for user ID: {}", userId);
        Page<SubscriptionResponse> subscriptions = subscriptionService.getUserSubscriptions(userId, pageable);
        return ResponseUtils.okPage(subscriptions);
    }

    /**
     * Get user's active subscription
     * GET /api/v1/subscriptions/active
     * Requires: Authenticated user
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getActiveSubscription() {
        Long userId = getCurrentUserId();
        log.debug("Fetching active subscription for user ID: {}", userId);
        SubscriptionResponse subscription = subscriptionService.getActiveSubscription(userId);
        if (subscription == null) {
            return ResponseUtils.ok(null, "No active subscription found");
        }
        return ResponseUtils.ok(subscription);
    }

    /**
     * Cancel a subscription
     * POST /api/v1/subscriptions/{id}/cancel
     * Requires: User owns the subscription OR ADMIN role
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean immediately) {
        log.info("Subscription cancellation request received for subscription ID: {}, immediately: {}", id,
                immediately);

        // Verify ownership
        SubscriptionResponse subscription = subscriptionService.getSubscription(id);
        Long userId = getCurrentUserId();
        if (!subscription.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only cancel your own subscriptions");
        }

        SubscriptionResponse response = subscriptionService.cancelSubscription(id, immediately);
        return ResponseUtils.ok(response,
                immediately ? "Subscription cancelled immediately" : "Subscription will be cancelled at period end");
    }

    /**
     * Update a subscription (plan change, cancel at period end setting)
     * PUT /api/v1/subscriptions/{id}
     * Requires: User owns the subscription OR ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionUpdateRequest request) {
        log.info("Subscription update request received for subscription ID: {}", id);

        // Verify ownership
        SubscriptionResponse subscription = subscriptionService.getSubscription(id);
        Long userId = getCurrentUserId();
        if (!subscription.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only update your own subscriptions");
        }

        SubscriptionResponse response = subscriptionService.updateSubscription(id, request);
        return ResponseUtils.ok(response, "Subscription updated successfully");
    }

    /**
     * Reactivate a cancelled subscription
     * POST /api/v1/subscriptions/{id}/reactivate
     * Requires: User owns the subscription OR ADMIN role
     */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> reactivateSubscription(@PathVariable Long id) {
        log.info("Subscription reactivation request received for subscription ID: {}", id);

        // Verify ownership
        SubscriptionResponse subscription = subscriptionService.getSubscription(id);
        Long userId = getCurrentUserId();
        if (!subscription.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only reactivate your own subscriptions");
        }

        SubscriptionResponse response = subscriptionService.reactivateSubscription(id);
        return ResponseUtils.ok(response, "Subscription reactivated successfully");
    }

    /**
     * Get subscription status
     * GET /api/v1/subscriptions/{id}/status
     * Requires: User owns the subscription OR ADMIN role
     */
    @GetMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getSubscriptionStatus(@PathVariable Long id) {
        log.debug("Fetching subscription status for ID: {}", id);

        SubscriptionResponse subscription = subscriptionService.getSubscription(id);
        Long userId = getCurrentUserId();
        if (!subscription.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own subscription status");
        }

        SubscriptionStatusResponse response = subscriptionService.getSubscriptionStatus(id);
        return ResponseUtils.ok(response);
    }
}
