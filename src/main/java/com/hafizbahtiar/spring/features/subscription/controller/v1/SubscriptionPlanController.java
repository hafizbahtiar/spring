package com.hafizbahtiar.spring.features.subscription.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionPlanRequest;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionPlanResponse;
import com.hafizbahtiar.spring.features.subscription.entity.BillingCycle;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlanType;
import com.hafizbahtiar.spring.features.subscription.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for subscription plan management endpoints.
 * Handles plan CRUD operations (admin-manageable dynamic plans).
 */
@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    /**
     * Create a new subscription plan (admin only)
     * POST /api/v1/subscription-plans
     * Requires: OWNER or ADMIN role
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(
            @Valid @RequestBody SubscriptionPlanRequest request) {
        log.info("Subscription plan creation request received: {}", request.getName());
        SubscriptionPlanResponse response = subscriptionPlanService.createPlan(request);
        return ResponseUtils.created(response, "Subscription plan created successfully");
    }

    /**
     * Update an existing subscription plan (admin only)
     * PUT /api/v1/subscription-plans/{id}
     * Requires: OWNER or ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request) {
        log.info("Subscription plan update request received for plan ID: {}", id);
        SubscriptionPlanResponse response = subscriptionPlanService.updatePlan(id, request);
        return ResponseUtils.ok(response, "Subscription plan updated successfully");
    }

    /**
     * Get subscription plan by ID (public endpoint)
     * GET /api/v1/subscription-plans/{id}
     * Requires: None (public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlan(@PathVariable Long id) {
        log.debug("Fetching subscription plan with ID: {}", id);
        SubscriptionPlanResponse response = subscriptionPlanService.getPlan(id);
        return ResponseUtils.ok(response);
    }

    /**
     * List all active subscription plans (public endpoint)
     * GET /api/v1/subscription-plans
     * Requires: None (public)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> listActivePlans() {
        log.debug("Listing all active subscription plans");
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.listActivePlans();
        return ResponseUtils.ok(plans);
    }

    /**
     * List subscription plans by plan type (public endpoint)
     * GET /api/v1/subscription-plans/type/{planType}
     * Requires: None (public)
     */
    @GetMapping("/type/{planType}")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> listPlansByType(
            @PathVariable SubscriptionPlanType planType) {
        log.debug("Listing subscription plans by type: {}", planType);
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.listPlansByType(planType);
        return ResponseUtils.ok(plans);
    }

    /**
     * List subscription plans by billing cycle (public endpoint)
     * GET /api/v1/subscription-plans/billing-cycle/{billingCycle}
     * Requires: None (public)
     */
    @GetMapping("/billing-cycle/{billingCycle}")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> listPlansByBillingCycle(
            @PathVariable BillingCycle billingCycle) {
        log.debug("Listing subscription plans by billing cycle: {}", billingCycle);
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.listPlansByBillingCycle(billingCycle);
        return ResponseUtils.ok(plans);
    }

    /**
     * Deactivate a subscription plan (admin only)
     * POST /api/v1/subscription-plans/{id}/deactivate
     * Requires: OWNER or ADMIN role
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivatePlan(@PathVariable Long id) {
        log.info("Subscription plan deactivation request received for plan ID: {}", id);
        subscriptionPlanService.deactivatePlan(id);
        return ResponseUtils.ok(null, "Subscription plan deactivated successfully");
    }

    /**
     * Activate a subscription plan (admin only)
     * POST /api/v1/subscription-plans/{id}/activate
     * Requires: OWNER or ADMIN role
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activatePlan(@PathVariable Long id) {
        log.info("Subscription plan activation request received for plan ID: {}", id);
        subscriptionPlanService.activatePlan(id);
        return ResponseUtils.ok(null, "Subscription plan activated successfully");
    }
}
