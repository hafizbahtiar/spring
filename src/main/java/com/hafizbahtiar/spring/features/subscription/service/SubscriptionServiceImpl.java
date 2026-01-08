package com.hafizbahtiar.spring.features.subscription.service;

import com.hafizbahtiar.spring.features.subscription.exception.SubscriptionException;
import com.hafizbahtiar.spring.features.subscription.exception.SubscriptionNotFoundException;
import com.hafizbahtiar.spring.features.subscription.exception.SubscriptionPlanNotFoundException;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.payment.dto.PaymentRequest;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.entity.PaymentStatus;
import com.hafizbahtiar.spring.features.payment.provider.PaymentProviderService;
import com.hafizbahtiar.spring.features.payment.service.PaymentMethodService;
import com.hafizbahtiar.spring.features.payment.service.PaymentService;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionRequest;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionResponse;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionStatusResponse;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionUpdateRequest;
import com.hafizbahtiar.spring.features.subscription.entity.Subscription;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlan;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionStatus;
import com.hafizbahtiar.spring.features.subscription.mapper.SubscriptionMapper;
import com.hafizbahtiar.spring.features.subscription.provider.SubscriptionProviderService;
import com.hafizbahtiar.spring.features.subscription.repository.SubscriptionPlanRepository;
import com.hafizbahtiar.spring.features.subscription.repository.SubscriptionRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of SubscriptionService.
 * Handles subscription lifecycle, payments, and status updates.
 * Integrates with PaymentService for subscription payments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserRepository userRepository;
    private final PaymentService paymentService; // For subscription payments
    private final PaymentMethodService paymentMethodService; // For getting default payment method
    private final List<SubscriptionProviderService> subscriptionProviderServices; // Injected list of all providers
    private final List<PaymentProviderService> paymentProviderServices; // For creating/retrieving customers
    private final SubscriptionLoggingService subscriptionLoggingService;

    /**
     * Get the appropriate subscription provider service for a given provider
     */
    private SubscriptionProviderService getProviderService(PaymentProvider provider) {
        return subscriptionProviderServices.stream()
                .filter(service -> service.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new SubscriptionException("Subscription provider not supported: " + provider));
    }

    /**
     * Get the appropriate payment provider service for customer operations
     */
    private PaymentProviderService getPaymentProviderService(PaymentProvider provider) {
        return paymentProviderServices.stream()
                .filter(service -> service.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new SubscriptionException("Payment provider not supported: " + provider));
    }

    /**
     * Get current HttpServletRequest for logging context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not retrieve current request: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public SubscriptionResponse createSubscription(Long userId, SubscriptionRequest request) {
        log.debug("Creating subscription for user ID: {}, plan ID: {}", userId, request.getPlanId());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate plan exists and is active
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> SubscriptionPlanNotFoundException.byId(request.getPlanId()));

        if (!plan.getIsActive()) {
            throw SubscriptionException.creationFailed("Plan is not active: " + request.getPlanId());
        }

        // Check if user already has an active subscription
        List<SubscriptionStatus> activeStatuses = Arrays.asList(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING);
        subscriptionRepository.findByUserIdAndStatusIn(userId, activeStatuses)
                .ifPresent(existing -> {
                    throw SubscriptionException.alreadyExists(userId);
                });

        // Create subscription entity
        PaymentProvider provider = PaymentProvider.STRIPE; // TODO: Allow provider selection
        Subscription subscription = new Subscription(user, plan, provider);

        // Set trial period if requested
        if (request.getTrialDays() != null && request.getTrialDays() > 0) {
            LocalDateTime now = LocalDateTime.now();
            subscription.setTrialStart(now);
            subscription.setTrialEnd(now.plusDays(request.getTrialDays()));
            subscription.setStatus(SubscriptionStatus.TRIALING);
        } else {
            subscription.setStatus(SubscriptionStatus.INCOMPLETE);
        }

        // Set cancel at period end if requested
        if (Boolean.TRUE.equals(request.getCancelAtPeriodEnd())) {
            subscription.setCancelAtPeriodEnd(true);
        }

        // Get provider service
        SubscriptionProviderService providerService = getProviderService(provider);
        PaymentProviderService paymentProviderService = getPaymentProviderService(provider);

        // Create or retrieve Stripe customer
        String providerCustomerId = paymentProviderService.createOrRetrieveCustomer(
                user.getEmail(),
                Map.of("user_id", userId.toString()));

        subscription.setProviderCustomerId(providerCustomerId);

        // Ensure plan has a Stripe Price ID
        String providerPlanId = plan.getProviderPlanId();
        if (providerPlanId == null || providerPlanId.isEmpty()) {
            // Create Stripe Price for the plan
            providerPlanId = providerService.createOrUpdatePrice(
                    plan.getName(),
                    plan.getPrice(),
                    plan.getCurrency(),
                    plan.getBillingCycle().getValue(),
                    null);
            plan.setProviderPlanId(providerPlanId);
            subscriptionPlanRepository.save(plan);
        }

        // Prepare trial end date
        LocalDate trialEndDate = null;
        if (request.getTrialDays() != null && request.getTrialDays() > 0) {
            trialEndDate = LocalDate.now().plusDays(request.getTrialDays());
        }

        // Create subscription with provider
        SubscriptionProviderService.ProviderSubscriptionResult providerResult = providerService.createSubscription(
                providerCustomerId,
                providerPlanId,
                null, // Payment method ID (can be set later)
                trialEndDate,
                subscription.getCancelAtPeriodEnd(),
                Map.of("user_id", userId.toString(), "plan_id", plan.getId().toString()));

        // Update subscription with provider result
        subscription.setProviderSubscriptionId(providerResult.getProviderSubscriptionId());
        subscription.setStatus(providerResult.getStatus());
        if (providerResult.getCurrentPeriodStart() != null) {
            subscription.setCurrentPeriodStart(providerResult.getCurrentPeriodStart().atStartOfDay());
        }
        if (providerResult.getCurrentPeriodEnd() != null) {
            subscription.setCurrentPeriodEnd(providerResult.getCurrentPeriodEnd().atStartOfDay());
        }
        if (providerResult.getTrialStart() != null) {
            subscription.setTrialStart(providerResult.getTrialStart().atStartOfDay());
        }
        if (providerResult.getTrialEnd() != null) {
            subscription.setTrialEnd(providerResult.getTrialEnd().atStartOfDay());
        }
        if (providerResult.getNextBillingDate() != null) {
            subscription.setNextBillingDate(providerResult.getNextBillingDate().atStartOfDay());
        }
        subscription.setCancelAtPeriodEnd(providerResult.isCancelAtPeriodEnd());

        long startTime = System.currentTimeMillis();
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info(
                "Subscription created successfully with ID: {}, user ID: {}, plan ID: {}, provider subscription ID: {}",
                savedSubscription.getId(), userId, request.getPlanId(), savedSubscription.getProviderSubscriptionId());

        // Log subscription creation
        subscriptionLoggingService.logSubscriptionCreated(
                savedSubscription.getId(),
                userId,
                plan.getId(),
                provider.name(),
                savedSubscription.getProviderSubscriptionId(),
                plan.getName(),
                savedSubscription.getStatus().name(),
                request.getTrialDays(),
                savedSubscription.getTrialStart(),
                savedSubscription.getTrialEnd(),
                getCurrentRequest(),
                responseTime);

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    public SubscriptionResponse cancelSubscription(Long subscriptionId, boolean cancelImmediately) {
        log.debug("Cancelling subscription ID: {}, immediately: {}", subscriptionId, cancelImmediately);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byId(subscriptionId));

        if (!subscription.canBeCancelled()) {
            throw SubscriptionException
                    .invalidState("Subscription cannot be cancelled in status: " + subscription.getStatus());
        }

        // Cancel with provider
        SubscriptionProviderService providerService = getProviderService(subscription.getProvider());
        SubscriptionProviderService.ProviderSubscriptionResult providerResult = providerService.cancelSubscription(
                subscription.getProviderSubscriptionId(),
                cancelImmediately);

        // Update subscription with provider result
        subscription.setStatus(providerResult.getStatus());
        subscription.setCancelAtPeriodEnd(providerResult.isCancelAtPeriodEnd());
        if (cancelImmediately) {
            subscription.cancelImmediately();
        } else {
            subscription.cancelAtPeriodEnd();
        }

        long startTime = System.currentTimeMillis();
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Subscription cancelled successfully with ID: {}, provider subscription ID: {}",
                subscriptionId, subscription.getProviderSubscriptionId());

        // Log subscription cancellation
        subscriptionLoggingService.logSubscriptionCancelled(
                updatedSubscription.getId(),
                updatedSubscription.getUser().getId(),
                updatedSubscription.getSubscriptionPlan().getId(),
                updatedSubscription.getProvider().name(),
                updatedSubscription.getProviderSubscriptionId(),
                updatedSubscription.getSubscriptionPlan().getName(),
                subscription.getStatus().name(), // Previous status
                cancelImmediately,
                getCurrentRequest(),
                responseTime);

        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse updateSubscription(Long subscriptionId, SubscriptionUpdateRequest request) {
        log.debug("Updating subscription ID: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byId(subscriptionId));

        // Store previous values for logging
        Long previousPlanId = subscription.getSubscriptionPlan().getId();
        String previousStatus = subscription.getStatus().name();

        // Update with provider
        SubscriptionProviderService providerService = getProviderService(subscription.getProvider());

        String newPriceId = null;
        Long newPlanId = null;
        if (request.getPlanId() != null) {
            SubscriptionPlan newPlan = subscriptionPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> SubscriptionPlanNotFoundException.byId(request.getPlanId()));

            if (!newPlan.getIsActive()) {
                throw SubscriptionException.updateFailed("Plan is not active: " + request.getPlanId());
            }

            // Ensure new plan has Stripe Price ID
            if (newPlan.getProviderPlanId() == null || newPlan.getProviderPlanId().isEmpty()) {
                String providerPlanId = providerService.createOrUpdatePrice(
                        newPlan.getName(),
                        newPlan.getPrice(),
                        newPlan.getCurrency(),
                        newPlan.getBillingCycle().getValue(),
                        null);
                newPlan.setProviderPlanId(providerPlanId);
                subscriptionPlanRepository.save(newPlan);
            }

            newPriceId = newPlan.getProviderPlanId();
            newPlanId = newPlan.getId();
            subscription.setSubscriptionPlan(newPlan);
            subscription.setBillingCycle(newPlan.getBillingCycle());
        }

        // Update subscription with provider
        SubscriptionProviderService.ProviderSubscriptionResult providerResult = providerService.updateSubscription(
                subscription.getProviderSubscriptionId(),
                newPriceId,
                request.getCancelAtPeriodEnd(),
                null);

        // Update subscription with provider result
        subscription.setStatus(providerResult.getStatus());
        if (providerResult.getCurrentPeriodStart() != null) {
            subscription.setCurrentPeriodStart(providerResult.getCurrentPeriodStart().atStartOfDay());
        }
        if (providerResult.getCurrentPeriodEnd() != null) {
            subscription.setCurrentPeriodEnd(providerResult.getCurrentPeriodEnd().atStartOfDay());
        }
        if (providerResult.getNextBillingDate() != null) {
            subscription.setNextBillingDate(providerResult.getNextBillingDate().atStartOfDay());
        }
        if (request.getCancelAtPeriodEnd() != null) {
            subscription.setCancelAtPeriodEnd(request.getCancelAtPeriodEnd());
        }

        long startTime = System.currentTimeMillis();
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Subscription updated successfully with ID: {}, provider subscription ID: {}",
                subscriptionId, subscription.getProviderSubscriptionId());

        // Log subscription update
        subscriptionLoggingService.logSubscriptionUpdated(
                updatedSubscription.getId(),
                updatedSubscription.getUser().getId(),
                updatedSubscription.getSubscriptionPlan().getId(),
                updatedSubscription.getProvider().name(),
                updatedSubscription.getProviderSubscriptionId(),
                updatedSubscription.getSubscriptionPlan().getName(),
                updatedSubscription.getStatus().name(),
                previousPlanId,
                newPlanId,
                previousStatus,
                updatedSubscription.getStatus().name(),
                getCurrentRequest(),
                responseTime);

        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse renewSubscription(Long subscriptionId) {
        log.debug("Renewing subscription ID: {}", subscriptionId);
        long startTime = System.currentTimeMillis();

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byId(subscriptionId));

        // Validate subscription can be renewed
        if (!subscription.isActive() && subscription.getStatus() != SubscriptionStatus.PAST_DUE) {
            throw SubscriptionException
                    .invalidState("Subscription cannot be renewed in status: " + subscription.getStatus());
        }

        // Check if subscription is set to cancel at period end
        if (subscription.getCancelAtPeriodEnd()) {
            throw SubscriptionException
                    .invalidState("Subscription is set to cancel at period end and cannot be renewed");
        }

        SubscriptionPlan plan = subscription.getSubscriptionPlan();
        Long userId = subscription.getUser().getId();

        // Get default payment method for the user
        var defaultPaymentMethod = paymentMethodService.getDefaultPaymentMethod(userId);
        if (defaultPaymentMethod == null) {
            throw SubscriptionException.renewalFailed("No default payment method found for user");
        }

        // Process renewal payment
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(plan.getPrice());
        paymentRequest.setCurrency(plan.getCurrency());
        paymentRequest.setPaymentMethodId(defaultPaymentMethod.getId());
        paymentRequest.setSubscriptionId(subscriptionId);
        paymentRequest.setDescription(String.format("Subscription renewal for %s", plan.getName()));
        paymentRequest.setSavePaymentMethod(false); // Already saved

        try {
            // Create and confirm payment
            var paymentResponse = paymentService.processPayment(userId, paymentRequest);

            // Confirm payment immediately (for subscription renewals, payment should be
            // automatic)
            if (paymentResponse.getStatus() != PaymentStatus.COMPLETED) {
                paymentResponse = paymentService.confirmPayment(paymentResponse.getId(), defaultPaymentMethod.getId());
            }

            // Check if payment was successful
            if (paymentResponse.getStatus() != PaymentStatus.COMPLETED) {
                throw SubscriptionException.renewalFailed("Payment failed with status: " + paymentResponse.getStatus());
            }

            // Calculate new billing period dates
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newPeriodStart = subscription.getCurrentPeriodEnd() != null
                    ? subscription.getCurrentPeriodEnd()
                    : now;
            LocalDateTime newPeriodEnd = newPeriodStart.plus(
                    plan.getBillingCycle().getMonths(),
                    plan.getBillingCycle().getUnit());

            // Update subscription period dates
            subscription.setCurrentPeriodStart(newPeriodStart);
            subscription.setCurrentPeriodEnd(newPeriodEnd);
            subscription.setNextBillingDate(newPeriodEnd);

            // Update subscription status to ACTIVE if it was PAST_DUE
            if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
                subscription.markAsActive();
            }

            Subscription updatedSubscription = subscriptionRepository.save(subscription);
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("Subscription renewed successfully with ID: {}, payment ID: {}, new period: {} to {}",
                    subscriptionId, paymentResponse.getId(), newPeriodStart, newPeriodEnd);

            // Log subscription renewal
            subscriptionLoggingService.logSubscriptionRenewed(
                    updatedSubscription.getId(),
                    userId,
                    plan.getId(),
                    subscription.getProvider().name(),
                    subscription.getProviderSubscriptionId(),
                    plan.getName(),
                    updatedSubscription.getStatus().name(),
                    paymentResponse.getId(),
                    paymentResponse.getProviderPaymentId(),
                    plan.getPrice(),
                    plan.getCurrency(),
                    getCurrentRequest(),
                    responseTime);

            return subscriptionMapper.toResponse(updatedSubscription);

        } catch (Exception e) {
            log.error("Failed to renew subscription ID: {}", subscriptionId, e);

            // Handle payment failure
            handlePaymentFailure(subscriptionId, "Renewal payment failed: " + e.getMessage());

            throw SubscriptionException.renewalFailed("Renewal failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(Long subscriptionId) {
        log.debug("Fetching subscription with ID: {}", subscriptionId);
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byId(subscriptionId));
        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionByProviderId(String providerSubscriptionId) {
        log.debug("Fetching subscription by provider subscription ID: {}", providerSubscriptionId);
        Subscription subscription = subscriptionRepository.findByProviderSubscriptionId(providerSubscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byProviderId(providerSubscriptionId));
        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getUserSubscriptions(Long userId, Pageable pageable) {
        log.debug("Listing subscriptions for user ID: {}", userId);
        Page<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return subscriptionMapper.toResponsePage(subscriptions);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveSubscription(Long userId) {
        log.debug("Fetching active subscription for user ID: {}", userId);
        List<SubscriptionStatus> activeStatuses = Arrays.asList(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING);
        return subscriptionRepository.findByUserIdAndStatusIn(userId, activeStatuses)
                .map(subscriptionMapper::toResponse)
                .orElse(null);
    }

    @Override
    public void updateSubscriptionStatus(String providerSubscriptionId, SubscriptionStatus status) {
        log.debug("Updating subscription status for provider subscription ID: {}, status: {}",
                providerSubscriptionId, status);

        Subscription subscription = subscriptionRepository.findByProviderSubscriptionId(providerSubscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byProviderId(providerSubscriptionId));

        String previousStatus = subscription.getStatus().name();
        subscription.setStatus(status);
        subscriptionRepository.save(subscription);

        log.info("Subscription status updated successfully for provider subscription ID: {}, status: {}",
                providerSubscriptionId, status);

        // Log status update
        subscriptionLoggingService.logStatusUpdate(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getSubscriptionPlan().getId(),
                subscription.getProvider().name(),
                subscription.getProviderSubscriptionId(),
                subscription.getSubscriptionPlan().getName(),
                previousStatus,
                status.name(),
                getCurrentRequest());
    }

    @Override
    public void handlePaymentSuccess(Long subscriptionId, Long paymentId) {
        log.debug("Handling payment success for subscription ID: {}, payment ID: {}", subscriptionId, paymentId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byId(subscriptionId));

        // Update subscription status to ACTIVE if it was TRIALING or INCOMPLETE
        if (subscription.getStatus() == SubscriptionStatus.TRIALING ||
                subscription.getStatus() == SubscriptionStatus.INCOMPLETE) {
            subscription.markAsActive();
        } else if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
            // Reactivate past due subscription
            subscription.markAsActive();
        }

        // Update next billing date based on billing cycle
        if (subscription.getCurrentPeriodEnd() != null) {
            subscription.setNextBillingDate(subscription.getCurrentPeriodEnd());
        }

        subscriptionRepository.save(subscription);
        log.info("Payment success handled for subscription ID: {}", subscriptionId);

        // Log payment success
        subscriptionLoggingService.logPaymentSuccess(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getSubscriptionPlan().getId(),
                subscription.getProvider().name(),
                subscription.getProviderSubscriptionId(),
                subscription.getSubscriptionPlan().getName(),
                subscription.getStatus().name(),
                paymentId,
                null, // Provider payment ID (can be retrieved from payment if needed)
                subscription.getSubscriptionPlan().getPrice(),
                subscription.getSubscriptionPlan().getCurrency(),
                getCurrentRequest());
    }

    @Override
    public void handlePaymentFailure(Long subscriptionId, String reason) {
        log.debug("Handling payment failure for subscription ID: {}, reason: {}", subscriptionId, reason);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byId(subscriptionId));

        // Update subscription status to PAST_DUE
        subscription.markAsPastDue();

        subscriptionRepository.save(subscription);
        log.warn("Payment failure handled for subscription ID: {}, reason: {}", subscriptionId, reason);

        // Log payment failure
        subscriptionLoggingService.logPaymentFailure(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getSubscriptionPlan().getId(),
                subscription.getProvider().name(),
                subscription.getProviderSubscriptionId(),
                subscription.getSubscriptionPlan().getName(),
                subscription.getStatus().name(),
                reason,
                null, // Failure code (can be extracted from reason if needed)
                getCurrentRequest());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getSubscriptionStatus(Long subscriptionId) {
        log.debug("Fetching subscription status for ID: {}", subscriptionId);
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byId(subscriptionId));
        return subscriptionMapper.toStatusResponse(subscription);
    }

    @Override
    public SubscriptionResponse reactivateSubscription(Long subscriptionId) {
        log.debug("Reactivating subscription ID: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> SubscriptionNotFoundException.byId(subscriptionId));

        // Reactivate with provider (update subscription to remove cancellation)
        SubscriptionProviderService providerService = getProviderService(subscription.getProvider());

        // Update subscription to remove cancel at period end
        SubscriptionProviderService.ProviderSubscriptionResult providerResult = providerService.updateSubscription(
                subscription.getProviderSubscriptionId(),
                null, // No plan change
                false, // Remove cancel at period end
                null);

        // Update subscription with provider result
        subscription.setStatus(providerResult.getStatus());
        subscription.setCancelAtPeriodEnd(false);
        subscription.setCancelledAt(null);
        if (providerResult.getCurrentPeriodStart() != null) {
            subscription.setCurrentPeriodStart(providerResult.getCurrentPeriodStart().atStartOfDay());
        }
        if (providerResult.getCurrentPeriodEnd() != null) {
            subscription.setCurrentPeriodEnd(providerResult.getCurrentPeriodEnd().atStartOfDay());
        }
        if (providerResult.getNextBillingDate() != null) {
            subscription.setNextBillingDate(providerResult.getNextBillingDate().atStartOfDay());
        }

        subscription.reactivate();

        long startTime = System.currentTimeMillis();
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Subscription reactivated successfully with ID: {}, provider subscription ID: {}",
                subscriptionId, subscription.getProviderSubscriptionId());

        // Log subscription reactivation
        subscriptionLoggingService.logSubscriptionReactivated(
                updatedSubscription.getId(),
                updatedSubscription.getUser().getId(),
                updatedSubscription.getSubscriptionPlan().getId(),
                updatedSubscription.getProvider().name(),
                updatedSubscription.getProviderSubscriptionId(),
                updatedSubscription.getSubscriptionPlan().getName(),
                updatedSubscription.getStatus().name(),
                getCurrentRequest(),
                responseTime);

        return subscriptionMapper.toResponse(updatedSubscription);
    }
}
