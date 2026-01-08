package com.hafizbahtiar.spring.features.payment.provider.stripe;

import com.hafizbahtiar.spring.common.exception.ProviderException;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.entity.PaymentStatus;
import com.hafizbahtiar.spring.features.payment.provider.PaymentProviderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe payment provider implementation.
 * Implements PaymentProviderService for Stripe integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProvider implements PaymentProviderService {

    private final StripeConfig stripeConfig;

    @PostConstruct
    public void init() {
        // Initialize Stripe API key
        Stripe.apiKey = stripeConfig.getSecretKey();
        log.info("Stripe payment provider initialized");
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public ProviderPaymentResult createPayment(
            BigDecimal amount,
            String currency,
            String customerId,
            String paymentMethodId,
            Map<String, String> metadata) {

        try {
            log.debug("Creating Stripe PaymentIntent: amount={}, currency={}, customerId={}",
                    amount, currency, customerId);

            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            // Build PaymentIntent creation parameters
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .putMetadata("source", "spring-saas-app");

            // Add customer if provided
            if (customerId != null) {
                paramsBuilder.setCustomer(customerId);
            }

            // Add payment method if provided
            if (paymentMethodId != null) {
                paramsBuilder.setPaymentMethod(paymentMethodId);
            }

            // Add metadata
            if (metadata != null && !metadata.isEmpty()) {
                paramsBuilder.putAllMetadata(metadata);
            }

            // Create PaymentIntent
            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            log.info("Stripe PaymentIntent created: {}", paymentIntent.getId());

            // Map Stripe status to our PaymentStatus
            PaymentStatus status = mapStripeStatusToPaymentStatus(paymentIntent.getStatus());

            // Build result
            ProviderPaymentResult result = new ProviderPaymentResult();
            result.setProviderPaymentId(paymentIntent.getId());
            result.setClientSecret(paymentIntent.getClientSecret());
            result.setStatus(status);

            // Store additional metadata
            Map<String, Object> resultMetadata = new HashMap<>();
            resultMetadata.put("stripe_payment_intent_id", paymentIntent.getId());
            resultMetadata.put("stripe_status", paymentIntent.getStatus());
            result.setMetadata(resultMetadata);

            return result;

        } catch (StripeException e) {
            log.error("Failed to create Stripe PaymentIntent: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Payment creation failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public ProviderPaymentResult confirmPayment(String providerPaymentId, String paymentMethodId) {
        try {
            log.debug("Confirming Stripe PaymentIntent: {}", providerPaymentId);

            // Retrieve PaymentIntent
            PaymentIntent paymentIntent = PaymentIntent.retrieve(providerPaymentId);

            // Build confirmation parameters
            PaymentIntentConfirmParams.Builder paramsBuilder = PaymentIntentConfirmParams.builder();

            // Add payment method if provided
            if (paymentMethodId != null) {
                paramsBuilder.setPaymentMethod(paymentMethodId);
            }

            // Confirm PaymentIntent
            paymentIntent = paymentIntent.confirm(paramsBuilder.build());

            log.info("Stripe PaymentIntent confirmed: {}, status: {}",
                    paymentIntent.getId(), paymentIntent.getStatus());

            // Map Stripe status to our PaymentStatus
            PaymentStatus status = mapStripeStatusToPaymentStatus(paymentIntent.getStatus());

            // Build result
            ProviderPaymentResult result = new ProviderPaymentResult();
            result.setProviderPaymentId(paymentIntent.getId());
            result.setClientSecret(paymentIntent.getClientSecret());
            result.setStatus(status);

            // Store additional metadata
            Map<String, Object> resultMetadata = new HashMap<>();
            resultMetadata.put("stripe_payment_intent_id", paymentIntent.getId());
            resultMetadata.put("stripe_status", paymentIntent.getStatus());
            if (paymentIntent.getLastPaymentError() != null) {
                resultMetadata.put("stripe_error", paymentIntent.getLastPaymentError().getMessage());
            }
            result.setMetadata(resultMetadata);

            return result;

        } catch (StripeException e) {
            log.error("Failed to confirm Stripe PaymentIntent: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Payment confirmation failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public PaymentStatus getPaymentStatus(String providerPaymentId) {
        try {
            log.debug("Getting Stripe PaymentIntent status: {}", providerPaymentId);
            PaymentIntent paymentIntent = PaymentIntent.retrieve(providerPaymentId);
            return mapStripeStatusToPaymentStatus(paymentIntent.getStatus());
        } catch (StripeException e) {
            log.error("Failed to get Stripe PaymentIntent status: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Failed to get payment status: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public ProviderRefundResult refundPayment(String providerPaymentId, BigDecimal amount, String reason) {
        try {
            log.debug("Processing Stripe refund: paymentIntentId={}, amount={}",
                    providerPaymentId, amount);

            // Get the charge ID from the PaymentIntent
            // In Stripe API, we need to search for charges related to this PaymentIntent
            String chargeId = null;

            // Use the Charge API to search for charges related to this PaymentIntent
            ChargeSearchParams chargeSearchParams = ChargeSearchParams.builder()
                    .setQuery("payment_intent:'" + providerPaymentId + "'")
                    .build();

            ChargeSearchResult chargeSearchResult = Charge.search(chargeSearchParams);
            if (chargeSearchResult.getData() != null && !chargeSearchResult.getData().isEmpty()) {
                // Get the first (most recent) charge
                chargeId = chargeSearchResult.getData().get(0).getId();
            }

            if (chargeId == null) {
                throw ProviderException.apiError("STRIPE", "No charge found for PaymentIntent: " + providerPaymentId);
            }

            // Build refund parameters
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setCharge(chargeId);

            // Set amount if partial refund
            if (amount != null) {
                long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
                paramsBuilder.setAmount(amountInCents);
            }

            // Add reason if provided
            if (reason != null) {
                paramsBuilder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
            }

            // Create refund
            Refund refund = Refund.create(paramsBuilder.build());

            log.info("Stripe refund created: {}, amount: {}", refund.getId(), refund.getAmount());

            // Refund amount in dollars
            BigDecimal refundAmount = BigDecimal.valueOf(refund.getAmount())
                    .divide(BigDecimal.valueOf(100));

            // Build result
            ProviderRefundResult result = new ProviderRefundResult();
            result.setProviderRefundId(refund.getId());
            result.setRefundAmount(refundAmount);
            result.setPaymentStatus(PaymentStatus.REFUNDED);

            return result;

        } catch (StripeException e) {
            log.error("Failed to process Stripe refund: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Refund failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public String createOrRetrieveCustomer(String email, Map<String, String> metadata) {
        try {
            log.debug("Creating or retrieving Stripe customer: email={}", email);

            // Search for existing customer by email
            CustomerSearchParams searchParams = CustomerSearchParams.builder()
                    .setQuery("email:'" + email + "'")
                    .build();

            CustomerSearchResult searchResult = Customer.search(searchParams);

            if (searchResult.getData() != null && !searchResult.getData().isEmpty()) {
                // Customer exists, return existing customer ID
                Customer existingCustomer = searchResult.getData().get(0);
                log.debug("Found existing Stripe customer: {}", existingCustomer.getId());
                return existingCustomer.getId();
            }

            // Create new customer
            CustomerCreateParams.Builder paramsBuilder = CustomerCreateParams.builder()
                    .setEmail(email);

            // Add metadata
            if (metadata != null && !metadata.isEmpty()) {
                paramsBuilder.putAllMetadata(metadata);
            }

            Customer customer = Customer.create(paramsBuilder.build());
            log.info("Created new Stripe customer: {}", customer.getId());

            return customer.getId();

        } catch (StripeException e) {
            log.error("Failed to create or retrieve Stripe customer: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Customer creation failed: " + e.getMessage(), e.getCode());
        }
    }

    @Override
    public void attachPaymentMethodToCustomer(String customerId, String paymentMethodId) {
        try {
            log.debug("Attaching Stripe PaymentMethod to customer: customerId={}, paymentMethodId={}",
                    customerId, paymentMethodId);

            // Retrieve PaymentMethod
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            // Attach to customer
            PaymentMethodUpdateParams updateParams = PaymentMethodUpdateParams.builder()
                    .putMetadata("customer_id", customerId)
                    .build();
            paymentMethod.update(updateParams);

            // Attach PaymentMethod to Customer
            PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build();
            paymentMethod.attach(attachParams);

            log.info("Stripe PaymentMethod attached to customer: customerId={}, paymentMethodId={}",
                    customerId, paymentMethodId);

        } catch (StripeException e) {
            log.error("Failed to attach Stripe PaymentMethod to customer: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Failed to attach payment method: " + e.getMessage(),
                    e.getCode());
        }
    }

    @Override
    public void detachPaymentMethodFromCustomer(String customerId, String paymentMethodId) {
        try {
            log.debug("Detaching Stripe PaymentMethod from customer: customerId={}, paymentMethodId={}",
                    customerId, paymentMethodId);

            // Retrieve PaymentMethod
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            // Verify payment method is attached to the customer
            if (paymentMethod.getCustomer() != null && !paymentMethod.getCustomer().equals(customerId)) {
                log.warn("PaymentMethod {} is attached to a different customer ({}), not {}",
                        paymentMethodId, paymentMethod.getCustomer(), customerId);
            }

            // Detach PaymentMethod from Customer
            PaymentMethodDetachParams detachParams = PaymentMethodDetachParams.builder().build();
            paymentMethod.detach(detachParams);

            log.info("Stripe PaymentMethod detached from customer: customerId={}, paymentMethodId={}",
                    customerId, paymentMethodId);

        } catch (StripeException e) {
            log.error("Failed to detach Stripe PaymentMethod from customer: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Failed to detach payment method: " + e.getMessage(),
                    e.getCode());
        }
    }

    @Override
    public ProviderPaymentMethodDetails getPaymentMethodDetails(String paymentMethodId) {
        try {
            log.debug("Retrieving Stripe PaymentMethod details: paymentMethodId={}", paymentMethodId);

            // Retrieve PaymentMethod from Stripe
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            // Create result object
            ProviderPaymentMethodDetails details = new ProviderPaymentMethodDetails();
            details.setType(paymentMethod.getType());

            // Extract card details if payment method type is "card"
            if ("card".equals(paymentMethod.getType()) && paymentMethod.getCard() != null) {
                PaymentMethod.Card card = paymentMethod.getCard();
                details.setLast4(card.getLast4());
                details.setBrand(card.getBrand());
                // Convert Long to Integer for expiry month/year
                if (card.getExpMonth() != null) {
                    details.setExpiryMonth(card.getExpMonth().intValue());
                }
                if (card.getExpYear() != null) {
                    details.setExpiryYear(card.getExpYear().intValue());
                }
            }

            // Set metadata if available
            if (paymentMethod.getMetadata() != null && !paymentMethod.getMetadata().isEmpty()) {
                Map<String, Object> metadata = new HashMap<>(paymentMethod.getMetadata());
                details.setMetadata(metadata);
            }

            log.debug("Retrieved Stripe PaymentMethod details: type={}, brand={}, last4={}",
                    details.getType(), details.getBrand(), details.getLast4());

            return details;

        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe PaymentMethod details: {}", e.getMessage(), e);
            throw ProviderException.apiError("STRIPE", "Failed to get payment method details: " + e.getMessage(),
                    e.getCode());
        }
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        try {
            log.debug("Validating Stripe webhook signature");

            // Stripe webhook signature validation
            // The signature header format is: t={timestamp},v1={signature}
            if (signature == null || stripeConfig.getWebhookSecret() == null) {
                log.warn("Webhook signature or secret not configured");
                return false;
            }

            // Use Stripe's webhook signature verification
            com.stripe.net.Webhook.constructEvent(
                    payload,
                    signature,
                    stripeConfig.getWebhookSecret());

            log.debug("Stripe webhook signature validated successfully");
            return true;

        } catch (Exception e) {
            log.error("Stripe webhook signature validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Map Stripe PaymentIntent status to our PaymentStatus enum.
     */
    private PaymentStatus mapStripeStatusToPaymentStatus(String stripeStatus) {
        return switch (stripeStatus.toLowerCase()) {
            case "requires_payment_method", "requires_confirmation", "requires_action" -> PaymentStatus.PENDING;
            case "processing" -> PaymentStatus.PROCESSING;
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "requires_capture" -> PaymentStatus.PROCESSING; // For manual capture
            case "canceled" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.FAILED;
        };
    }
}
