package com.hafizbahtiar.spring.features.subscription.provider;

import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionStatus;

import java.time.LocalDate;
import java.util.Map;

/**
 * Interface for subscription provider abstraction.
 * Allows decoupling subscription services from specific provider
 * implementations (Stripe, PayPal, etc.).
 */
public interface SubscriptionProviderService {

    /**
     * Get the payment provider this service handles.
     * 
     * @return PaymentProvider enum value
     */
    PaymentProvider getProvider();

    /**
     * Create a subscription with the provider.
     * 
     * @param customerId        Provider-specific customer ID (e.g., Stripe Customer
     *                          ID)
     * @param priceId           Provider-specific price ID (e.g., Stripe Price ID)
     * @param paymentMethodId   Provider-specific payment method ID (optional)
     * @param trialEndDate      Trial end date (optional, null if no trial)
     * @param cancelAtPeriodEnd Whether to cancel at period end
     * @param metadata          Additional metadata
     * @return ProviderSubscriptionResult with subscription details
     */
    ProviderSubscriptionResult createSubscription(
            String customerId,
            String priceId,
            String paymentMethodId,
            LocalDate trialEndDate,
            boolean cancelAtPeriodEnd,
            Map<String, String> metadata);

    /**
     * Cancel a subscription.
     * 
     * @param subscriptionId    Provider-specific subscription ID
     * @param cancelImmediately Whether to cancel immediately or at period end
     * @return Updated subscription details
     */
    ProviderSubscriptionResult cancelSubscription(String subscriptionId, boolean cancelImmediately);

    /**
     * Update a subscription (plan change, billing cycle change, etc.).
     * 
     * @param subscriptionId    Provider-specific subscription ID
     * @param newPriceId        New price ID (optional, for plan changes)
     * @param cancelAtPeriodEnd Whether to cancel at period end (optional)
     * @param metadata          Additional metadata (optional)
     * @return Updated subscription details
     */
    ProviderSubscriptionResult updateSubscription(
            String subscriptionId,
            String newPriceId,
            Boolean cancelAtPeriodEnd,
            Map<String, String> metadata);

    /**
     * Retrieve subscription details from provider.
     * 
     * @param subscriptionId Provider-specific subscription ID
     * @return Subscription details
     */
    ProviderSubscriptionResult getSubscription(String subscriptionId);

    /**
     * Create or retrieve a Stripe Price for a subscription plan.
     * 
     * @param planName        Plan name
     * @param amount          Price amount
     * @param currency        Currency code
     * @param billingCycle    Billing cycle (monthly, yearly, etc.)
     * @param existingPriceId Existing price ID (if updating)
     * @return Provider Price ID
     */
    String createOrUpdatePrice(
            String planName,
            java.math.BigDecimal amount,
            String currency,
            String billingCycle,
            String existingPriceId);

    /**
     * Map provider subscription status to SubscriptionStatus enum.
     * 
     * @param providerStatus Provider-specific status string
     * @return SubscriptionStatus enum value
     */
    SubscriptionStatus mapProviderStatusToSubscriptionStatus(String providerStatus);

    /**
     * Result object for subscription operations.
     */
    class ProviderSubscriptionResult {
        private String providerSubscriptionId;
        private String providerCustomerId;
        private SubscriptionStatus status;
        private LocalDate currentPeriodStart;
        private LocalDate currentPeriodEnd;
        private LocalDate trialStart;
        private LocalDate trialEnd;
        private LocalDate nextBillingDate;
        private boolean cancelAtPeriodEnd;
        private String latestInvoiceId;
        private String clientSecret; // For payment confirmation if needed
        private Map<String, Object> metadata;

        // Getters and setters
        public String getProviderSubscriptionId() {
            return providerSubscriptionId;
        }

        public void setProviderSubscriptionId(String providerSubscriptionId) {
            this.providerSubscriptionId = providerSubscriptionId;
        }

        public String getProviderCustomerId() {
            return providerCustomerId;
        }

        public void setProviderCustomerId(String providerCustomerId) {
            this.providerCustomerId = providerCustomerId;
        }

        public SubscriptionStatus getStatus() {
            return status;
        }

        public void setStatus(SubscriptionStatus status) {
            this.status = status;
        }

        public LocalDate getCurrentPeriodStart() {
            return currentPeriodStart;
        }

        public void setCurrentPeriodStart(LocalDate currentPeriodStart) {
            this.currentPeriodStart = currentPeriodStart;
        }

        public LocalDate getCurrentPeriodEnd() {
            return currentPeriodEnd;
        }

        public void setCurrentPeriodEnd(LocalDate currentPeriodEnd) {
            this.currentPeriodEnd = currentPeriodEnd;
        }

        public LocalDate getTrialStart() {
            return trialStart;
        }

        public void setTrialStart(LocalDate trialStart) {
            this.trialStart = trialStart;
        }

        public LocalDate getTrialEnd() {
            return trialEnd;
        }

        public void setTrialEnd(LocalDate trialEnd) {
            this.trialEnd = trialEnd;
        }

        public LocalDate getNextBillingDate() {
            return nextBillingDate;
        }

        public void setNextBillingDate(LocalDate nextBillingDate) {
            this.nextBillingDate = nextBillingDate;
        }

        public boolean isCancelAtPeriodEnd() {
            return cancelAtPeriodEnd;
        }

        public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
            this.cancelAtPeriodEnd = cancelAtPeriodEnd;
        }

        public String getLatestInvoiceId() {
            return latestInvoiceId;
        }

        public void setLatestInvoiceId(String latestInvoiceId) {
            this.latestInvoiceId = latestInvoiceId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}
