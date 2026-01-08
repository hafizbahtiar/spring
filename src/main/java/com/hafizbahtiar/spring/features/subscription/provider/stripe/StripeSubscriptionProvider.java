package com.hafizbahtiar.spring.features.subscription.provider.stripe;

import com.hafizbahtiar.spring.common.exception.ProviderException;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.provider.stripe.StripeConfig;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionStatus;
import com.hafizbahtiar.spring.features.subscription.provider.SubscriptionProviderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe subscription provider implementation.
 * Handles all Stripe Subscriptions API operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeSubscriptionProvider implements SubscriptionProviderService {

    private final StripeConfig stripeConfig;

    @PostConstruct
    public void init() {
        // Stripe API key is already initialized by StripePaymentProvider
        // But we ensure it's set here too for safety
        if (Stripe.apiKey == null || Stripe.apiKey.isEmpty()) {
            Stripe.apiKey = stripeConfig.getSecretKey();
        }
        log.info("Stripe subscription provider initialized");
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public ProviderSubscriptionResult createSubscription(
            String customerId,
            String priceId,
            String paymentMethodId,
            LocalDate trialEndDate,
            boolean cancelAtPeriodEnd,
            Map<String, String> metadata) {

        try {
            log.debug("Creating Stripe Subscription: customerId={}, priceId={}, trialEnd={}",
                    customerId, priceId, trialEndDate);

            // Build subscription creation parameters
            SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                    .setCustomer(customerId)
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(priceId)
                            .build())
                    .putMetadata("source", "spring-saas-app");

            // Set trial end if provided
            if (trialEndDate != null) {
                long trialEndTimestamp = trialEndDate.atStartOfDay(ZoneId.systemDefault())
                        .toInstant().getEpochSecond();
                paramsBuilder.setTrialEnd(trialEndTimestamp);
            }

            // Set payment method if provided
            if (paymentMethodId != null) {
                paramsBuilder.setDefaultPaymentMethod(paymentMethodId);
            }

            // Set cancel at period end
            if (cancelAtPeriodEnd) {
                paramsBuilder.setCancelAtPeriodEnd(true);
            }

            // Add metadata
            if (metadata != null && !metadata.isEmpty()) {
                paramsBuilder.putAllMetadata(metadata);
            }

            // Create subscription
            Subscription subscription = Subscription.create(paramsBuilder.build());

            log.info("Stripe Subscription created: {}", subscription.getId());

            return mapStripeSubscriptionToResult(subscription);

        } catch (StripeException e) {
            log.error("Failed to create Stripe subscription: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Subscription creation failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public ProviderSubscriptionResult cancelSubscription(String subscriptionId, boolean cancelImmediately) {
        try {
            log.debug("Cancelling Stripe Subscription: subscriptionId={}, immediately={}",
                    subscriptionId, cancelImmediately);

            Subscription subscription;

            if (cancelImmediately) {
                // Cancel immediately
                subscription = Subscription.retrieve(subscriptionId);
                SubscriptionCancelParams cancelParams = SubscriptionCancelParams.builder().build();
                subscription = subscription.cancel(cancelParams);
            } else {
                // Cancel at period end
                SubscriptionUpdateParams updateParams = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();
                subscription = Subscription.retrieve(subscriptionId);
                subscription = subscription.update(updateParams);
            }

            log.info("Stripe Subscription cancelled: {}", subscriptionId);

            return mapStripeSubscriptionToResult(subscription);

        } catch (StripeException e) {
            log.error("Failed to cancel Stripe subscription: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Subscription cancellation failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public ProviderSubscriptionResult updateSubscription(
            String subscriptionId,
            String newPriceId,
            Boolean cancelAtPeriodEnd,
            Map<String, String> metadata) {

        try {
            log.debug("Updating Stripe Subscription: subscriptionId={}, newPriceId={}",
                    subscriptionId, newPriceId);

            Subscription subscription = Subscription.retrieve(subscriptionId);

            SubscriptionUpdateParams.Builder updateParamsBuilder = SubscriptionUpdateParams.builder();

            // Update price if provided
            if (newPriceId != null) {
                // Get current subscription items
                SubscriptionItemCollection items = subscription.getItems();
                if (items != null && !items.getData().isEmpty()) {
                    String subscriptionItemId = items.getData().get(0).getId();

                    // Update subscription item with new price
                    SubscriptionItemUpdateParams itemUpdateParams = SubscriptionItemUpdateParams.builder()
                            .setPrice(newPriceId)
                            .build();
                    SubscriptionItem.retrieve(subscriptionItemId).update(itemUpdateParams);
                } else {
                    // Add new item if no items exist
                    updateParamsBuilder.addItem(SubscriptionUpdateParams.Item.builder()
                            .setPrice(newPriceId)
                            .build());
                }
            }

            // Update cancel at period end if provided
            if (cancelAtPeriodEnd != null) {
                updateParamsBuilder.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            }

            // Add metadata if provided
            if (metadata != null && !metadata.isEmpty()) {
                updateParamsBuilder.putAllMetadata(metadata);
            }

            // Update subscription
            subscription = subscription.update(updateParamsBuilder.build());

            log.info("Stripe Subscription updated: {}", subscriptionId);

            return mapStripeSubscriptionToResult(subscription);

        } catch (StripeException e) {
            log.error("Failed to update Stripe subscription: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Subscription update failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public ProviderSubscriptionResult getSubscription(String subscriptionId) {
        try {
            log.debug("Retrieving Stripe Subscription: {}", subscriptionId);

            Subscription subscription = Subscription.retrieve(subscriptionId);

            return mapStripeSubscriptionToResult(subscription);

        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe subscription: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Subscription retrieval failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public String createOrUpdatePrice(
            String planName,
            BigDecimal amount,
            String currency,
            String billingCycle,
            String existingPriceId) {

        try {
            log.debug("Creating/updating Stripe Price: planName={}, amount={}, currency={}, billingCycle={}",
                    planName, amount, currency, billingCycle);

            // Convert amount to cents
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            // Map billing cycle to Stripe interval
            String interval;
            int intervalCount;
            switch (billingCycle.toUpperCase()) {
                case "MONTHLY":
                    interval = "month";
                    intervalCount = 1;
                    break;
                case "QUARTERLY":
                    interval = "month";
                    intervalCount = 3;
                    break;
                case "YEARLY":
                    interval = "year";
                    intervalCount = 1;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported billing cycle: " + billingCycle);
            }

            // If existing price ID provided, update it (Stripe doesn't support updating prices, so we create a new one)
            // For now, we'll always create a new price
            // In production, you might want to archive old prices and create new ones

            // First, create or retrieve a Product
            Product product = findOrCreateProduct(planName);

            // Create Price
            PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setProduct(product.getId())
                    .setUnitAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setRecurring(PriceCreateParams.Recurring.builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.valueOf(interval.toUpperCase()))
                            .setIntervalCount((long) intervalCount)
                            .build())
                    .putMetadata("plan_name", planName)
                    .putMetadata("billing_cycle", billingCycle)
                    .build();

            Price price = Price.create(priceParams);

            log.info("Stripe Price created: {} for plan: {}", price.getId(), planName);

            return price.getId();

        } catch (StripeException e) {
            log.error("Failed to create/update Stripe price: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Price creation failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public SubscriptionStatus mapProviderStatusToSubscriptionStatus(String providerStatus) {
        if (providerStatus == null) {
            return SubscriptionStatus.INCOMPLETE;
        }

        // Map Stripe subscription statuses to our SubscriptionStatus enum
        switch (providerStatus.toUpperCase()) {
            case "ACTIVE":
                return SubscriptionStatus.ACTIVE;
            case "TRIALING":
                return SubscriptionStatus.TRIALING;
            case "PAST_DUE":
                return SubscriptionStatus.PAST_DUE;
            case "CANCELED":
            case "CANCELLED":
                return SubscriptionStatus.CANCELLED;
            case "UNPAID":
                return SubscriptionStatus.UNPAID;
            case "INCOMPLETE":
                return SubscriptionStatus.INCOMPLETE;
            case "INCOMPLETE_EXPIRED":
                return SubscriptionStatus.INCOMPLETE_EXPIRED;
            default:
                log.warn("Unknown Stripe subscription status: {}, defaulting to INCOMPLETE", providerStatus);
                return SubscriptionStatus.INCOMPLETE;
        }
    }

    /**
     * Map Stripe Subscription object to ProviderSubscriptionResult.
     */
    private ProviderSubscriptionResult mapStripeSubscriptionToResult(Subscription subscription) {
        ProviderSubscriptionResult result = new ProviderSubscriptionResult();

        result.setProviderSubscriptionId(subscription.getId());
        result.setProviderCustomerId(subscription.getCustomer());

        // Map status
        result.setStatus(mapProviderStatusToSubscriptionStatus(subscription.getStatus()));

        // Map dates
        if (subscription.getCurrentPeriodStart() != null) {
            result.setCurrentPeriodStart(Instant.ofEpochSecond(subscription.getCurrentPeriodStart())
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }
        if (subscription.getCurrentPeriodEnd() != null) {
            result.setCurrentPeriodEnd(Instant.ofEpochSecond(subscription.getCurrentPeriodEnd())
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }
        if (subscription.getTrialStart() != null) {
            result.setTrialStart(Instant.ofEpochSecond(subscription.getTrialStart())
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }
        if (subscription.getTrialEnd() != null) {
            result.setTrialEnd(Instant.ofEpochSecond(subscription.getTrialEnd())
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }

        // Set cancel at period end
        result.setCancelAtPeriodEnd(subscription.getCancelAtPeriodEnd() != null && subscription.getCancelAtPeriodEnd());

        // Get latest invoice for next billing date
        if (subscription.getLatestInvoice() != null) {
            try {
                Invoice invoice = Invoice.retrieve(subscription.getLatestInvoice());
                result.setLatestInvoiceId(invoice.getId());
                if (invoice.getNextPaymentAttempt() != null) {
                    result.setNextBillingDate(Instant.ofEpochSecond(invoice.getNextPaymentAttempt())
                            .atZone(ZoneId.systemDefault()).toLocalDate());
                }
            } catch (StripeException e) {
                log.warn("Failed to retrieve latest invoice: {}", e.getMessage());
            }
        }

        // Set metadata
        Map<String, Object> metadata = new HashMap<>();
        if (subscription.getMetadata() != null) {
            metadata.putAll(subscription.getMetadata());
        }
        result.setMetadata(metadata);

        return result;
    }

    /**
     * Find or create a Stripe Product for the plan.
     */
    private Product findOrCreateProduct(String planName) throws StripeException {
        // Search for existing product
        ProductSearchParams searchParams = ProductSearchParams.builder()
                .setQuery("name:'" + planName + "' AND active:'true'")
                .build();

        ProductSearchResult searchResult = Product.search(searchParams);

        if (searchResult.getData() != null && !searchResult.getData().isEmpty()) {
            // Product exists, return it
            Product existingProduct = searchResult.getData().get(0);
            log.debug("Found existing Stripe product: {}", existingProduct.getId());
            return existingProduct;
        }

        // Create new product
        ProductCreateParams productParams = ProductCreateParams.builder()
                .setName(planName)
                .putMetadata("source", "spring-saas-app")
                .build();

        Product product = Product.create(productParams);
        log.info("Created new Stripe product: {} for plan: {}", product.getId(), planName);

        return product;
    }
}

