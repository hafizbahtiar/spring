package com.hafizbahtiar.spring.features.subscription.service;

import com.hafizbahtiar.spring.features.subscription.exception.SubscriptionPlanNotFoundException;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionPlanRequest;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionPlanResponse;
import com.hafizbahtiar.spring.features.subscription.entity.BillingCycle;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlan;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlanType;
import com.hafizbahtiar.spring.features.subscription.mapper.SubscriptionPlanMapper;
import com.hafizbahtiar.spring.features.subscription.provider.SubscriptionProviderService;
import com.hafizbahtiar.spring.features.subscription.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of SubscriptionPlanService.
 * Handles dynamic subscription plan management (admin-manageable plans).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final List<SubscriptionProviderService> subscriptionProviderServices; // For Stripe Price creation

    /**
     * Get Stripe subscription provider service
     */
    private SubscriptionProviderService getStripeProvider() {
        return subscriptionProviderServices.stream()
                .filter(service -> service.getProvider() == PaymentProvider.STRIPE)
                .findFirst()
                .orElse(null); // Return null if Stripe not available (for non-Stripe plans)
    }

    @Override
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        log.debug("Creating subscription plan: {}", request.getName());

        // Create plan entity
        SubscriptionPlan plan = new SubscriptionPlan(
                request.getName(),
                request.getDescription(),
                request.getPlanType(),
                request.getPrice(),
                request.getCurrency(),
                request.getBillingCycle());

        // Set optional fields
        if (request.getFeatures() != null) {
            plan.setFeatures(request.getFeatures());
        }
        if (request.getMaxUsers() != null) {
            plan.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxStorage() != null) {
            plan.setMaxStorage(request.getMaxStorage());
        }
        if (request.getProviderPlanId() != null) {
            plan.setProviderPlanId(request.getProviderPlanId());
        } else {
            // Auto-create Stripe Price if provider plan ID not provided and Stripe is
            // available
            SubscriptionProviderService stripeProvider = getStripeProvider();
            if (stripeProvider != null) {
                try {
                    String providerPlanId = stripeProvider.createOrUpdatePrice(
                            request.getName(),
                            request.getPrice(),
                            request.getCurrency(),
                            request.getBillingCycle().getValue(),
                            null);
                    plan.setProviderPlanId(providerPlanId);
                    log.info("Created Stripe Price for plan: {} with Price ID: {}", request.getName(), providerPlanId);
                } catch (Exception e) {
                    log.warn(
                            "Failed to create Stripe Price for plan: {}, plan will be saved without provider plan ID. Error: {}",
                            request.getName(), e.getMessage());
                }
            }
        }
        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }

        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);
        log.info("Subscription plan created successfully with ID: {}, name: {}", savedPlan.getId(),
                savedPlan.getName());

        return subscriptionPlanMapper.toResponse(savedPlan);
    }

    @Override
    public SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanRequest request) {
        log.debug("Updating subscription plan ID: {}", planId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> SubscriptionPlanNotFoundException.byId(planId));

        // Update fields
        if (request.getName() != null) {
            plan.setName(request.getName());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getFeatures() != null) {
            plan.setFeatures(request.getFeatures());
        }
        if (request.getMaxUsers() != null) {
            plan.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxStorage() != null) {
            plan.setMaxStorage(request.getMaxStorage());
        }
        if (request.getProviderPlanId() != null) {
            plan.setProviderPlanId(request.getProviderPlanId());
        }
        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }

        SubscriptionPlan updatedPlan = subscriptionPlanRepository.save(plan);
        log.info("Subscription plan updated successfully with ID: {}", updatedPlan.getId());

        return subscriptionPlanMapper.toResponse(updatedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlan(Long planId) {
        log.debug("Fetching subscription plan with ID: {}", planId);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> SubscriptionPlanNotFoundException.byId(planId));
        return subscriptionPlanMapper.toResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> listActivePlans() {
        log.debug("Listing all active subscription plans");
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByIsActiveTrueOrderByPlanTypeAscPriceAsc();
        return subscriptionPlanMapper.toResponseList(plans);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> listPlansByType(SubscriptionPlanType planType) {
        log.debug("Listing subscription plans by type: {}", planType);
        List<SubscriptionPlan> plans = subscriptionPlanRepository
                .findByPlanTypeAndIsActiveTrueOrderByPriceAsc(planType);
        return subscriptionPlanMapper.toResponseList(plans);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> listPlansByBillingCycle(BillingCycle billingCycle) {
        log.debug("Listing subscription plans by billing cycle: {}", billingCycle);
        List<SubscriptionPlan> plans = subscriptionPlanRepository
                .findByBillingCycleAndIsActiveTrueOrderByPriceAsc(billingCycle);
        return subscriptionPlanMapper.toResponseList(plans);
    }

    @Override
    public void deactivatePlan(Long planId) {
        log.debug("Deactivating subscription plan ID: {}", planId);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> SubscriptionPlanNotFoundException.byId(planId));

        // Validate plan has no active subscriptions
        // Note: We allow deactivation even with active subscriptions, but log a warning
        // Existing subscriptions will continue to work, but new subscriptions cannot
        // use this plan
        long activeSubscriptionCount = subscriptionPlanRepository.countActiveSubscriptionsByPlanId(planId);
        if (activeSubscriptionCount > 0) {
            log.warn("Deactivating subscription plan ID: {} with {} active subscription(s). " +
                    "Existing subscriptions will continue, but new subscriptions cannot use this plan.",
                    planId, activeSubscriptionCount);
            // Optionally, you can throw an exception here if you want to prevent
            // deactivation:
            // throw SubscriptionException.planHasActiveSubscriptions(planId,
            // activeSubscriptionCount);
        }

        plan.deactivate();
        subscriptionPlanRepository.save(plan);
        log.info("Subscription plan deactivated successfully with ID: {}", planId);
    }

    @Override
    public void activatePlan(Long planId) {
        log.debug("Activating subscription plan ID: {}", planId);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> SubscriptionPlanNotFoundException.byId(planId));

        plan.activate();
        subscriptionPlanRepository.save(plan);
        log.info("Subscription plan activated successfully with ID: {}", planId);
    }
}
