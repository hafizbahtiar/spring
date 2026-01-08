package com.hafizbahtiar.spring.features.payment.provider;

import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Abstraction interface for payment provider integrations.
 * Implementations: StripePaymentProvider, PayPalPaymentProvider
 * 
 * This interface allows the PaymentService to work with any payment provider
 * without being tightly coupled to a specific implementation.
 */
public interface PaymentProviderService {

    /**
     * Get the payment provider this service handles
     */
    PaymentProvider getProvider();

    /**
     * Create a payment intent/order with the provider.
     * For Stripe: Creates a PaymentIntent
     * For PayPal: Creates an Order
     * 
     * @param amount          Payment amount
     * @param currency        Currency code (ISO 4217)
     * @param customerId      Provider-specific customer ID (optional)
     * @param paymentMethodId Provider-specific payment method ID (optional)
     * @param metadata        Additional metadata
     * @return ProviderPaymentResult containing provider payment ID, client secret,
     *         redirect URL, etc.
     */
    ProviderPaymentResult createPayment(
            BigDecimal amount,
            String currency,
            String customerId,
            String paymentMethodId,
            Map<String, String> metadata);

    /**
     * Confirm a payment.
     * For Stripe: Confirms a PaymentIntent
     * For PayPal: Captures an Order
     * 
     * @param providerPaymentId Provider-specific payment ID
     * @param paymentMethodId   Optional payment method ID for confirmation
     * @return ProviderPaymentResult with updated status
     */
    ProviderPaymentResult confirmPayment(String providerPaymentId, String paymentMethodId);

    /**
     * Get payment status from provider.
     * 
     * @param providerPaymentId Provider-specific payment ID
     * @return PaymentStatus
     */
    PaymentStatus getPaymentStatus(String providerPaymentId);

    /**
     * Process a refund.
     * 
     * @param providerPaymentId Provider-specific payment ID
     * @param amount            Refund amount (null for full refund)
     * @param reason            Refund reason
     * @return ProviderRefundResult containing refund ID and status
     */
    ProviderRefundResult refundPayment(String providerPaymentId, BigDecimal amount, String reason);

    /**
     * Create or retrieve a customer in the provider system.
     * For Stripe: Creates/retrieves a Customer
     * For PayPal: Not applicable (uses payer ID)
     * 
     * @param email    Customer email
     * @param metadata Additional metadata
     * @return Provider customer ID
     */
    String createOrRetrieveCustomer(String email, Map<String, String> metadata);

    /**
     * Attach a payment method to a customer.
     * For Stripe: Attaches PaymentMethod to Customer
     * For PayPal: Not applicable
     * 
     * @param customerId      Provider customer ID
     * @param paymentMethodId Provider payment method ID
     */
    void attachPaymentMethodToCustomer(String customerId, String paymentMethodId);

    /**
     * Detach a payment method from a customer.
     * For Stripe: Detaches PaymentMethod from Customer
     * For PayPal: Not applicable
     * 
     * @param customerId      Provider customer ID (for validation)
     * @param paymentMethodId Provider payment method ID
     */
    void detachPaymentMethodFromCustomer(String customerId, String paymentMethodId);

    /**
     * Get payment method details from provider.
     * For Stripe: Retrieves PaymentMethod and extracts card details
     * For PayPal: Not applicable
     * 
     * @param paymentMethodId Provider payment method ID
     * @return ProviderPaymentMethodDetails with card details, type, etc.
     */
    ProviderPaymentMethodDetails getPaymentMethodDetails(String paymentMethodId);

    /**
     * Validate webhook signature.
     * 
     * @param payload   Webhook payload
     * @param signature Webhook signature header
     * @return true if signature is valid
     */
    boolean validateWebhookSignature(String payload, String signature);

    /**
     * Result class for payment creation/confirmation
     */
    class ProviderPaymentResult {
        private String providerPaymentId;
        private String clientSecret; // For Stripe PaymentIntent
        private String redirectUrl; // For PayPal redirect flow
        private PaymentStatus status;
        private Map<String, Object> metadata;

        public ProviderPaymentResult() {
        }

        public ProviderPaymentResult(String providerPaymentId, String clientSecret, PaymentStatus status) {
            this.providerPaymentId = providerPaymentId;
            this.clientSecret = clientSecret;
            this.status = status;
        }

        // Getters and setters
        public String getProviderPaymentId() {
            return providerPaymentId;
        }

        public void setProviderPaymentId(String providerPaymentId) {
            this.providerPaymentId = providerPaymentId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        public PaymentStatus getStatus() {
            return status;
        }

        public void setStatus(PaymentStatus status) {
            this.status = status;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * Result class for refund operations
     */
    class ProviderRefundResult {
        private String providerRefundId;
        private BigDecimal refundAmount;
        private PaymentStatus paymentStatus;

        public ProviderRefundResult() {
        }

        public ProviderRefundResult(String providerRefundId, BigDecimal refundAmount, PaymentStatus paymentStatus) {
            this.providerRefundId = providerRefundId;
            this.refundAmount = refundAmount;
            this.paymentStatus = paymentStatus;
        }

        // Getters and setters
        public String getProviderRefundId() {
            return providerRefundId;
        }

        public void setProviderRefundId(String providerRefundId) {
            this.providerRefundId = providerRefundId;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
        }
    }

    /**
     * Result class for payment method details from provider
     */
    class ProviderPaymentMethodDetails {
        private String type; // e.g., "card", "paypal"
        private String last4; // Last 4 digits of card
        private String brand; // Card brand (visa, mastercard, etc.)
        private Integer expiryMonth; // Card expiry month (1-12)
        private Integer expiryYear; // Card expiry year (4 digits)
        private Map<String, Object> metadata; // Additional metadata

        public ProviderPaymentMethodDetails() {
        }

        // Getters and setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLast4() {
            return last4;
        }

        public void setLast4(String last4) {
            this.last4 = last4;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public Integer getExpiryMonth() {
            return expiryMonth;
        }

        public void setExpiryMonth(Integer expiryMonth) {
            this.expiryMonth = expiryMonth;
        }

        public Integer getExpiryYear() {
            return expiryYear;
        }

        public void setExpiryYear(Integer expiryYear) {
            this.expiryYear = expiryYear;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}
