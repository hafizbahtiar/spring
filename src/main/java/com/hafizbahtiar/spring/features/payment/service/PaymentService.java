package com.hafizbahtiar.spring.features.payment.service;

import com.hafizbahtiar.spring.features.payment.dto.PaymentRequest;
import com.hafizbahtiar.spring.features.payment.dto.PaymentResponse;
import com.hafizbahtiar.spring.features.payment.dto.PaymentStatusResponse;
import com.hafizbahtiar.spring.features.payment.dto.RefundRequest;
import com.hafizbahtiar.spring.features.payment.dto.RefundResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for payment management.
 * Handles payment processing, confirmation, refunds, and status checks.
 * Uses PaymentProviderService abstraction for provider-specific logic.
 */
public interface PaymentService {

    /**
     * Process a payment (create payment intent).
     * For Stripe: Creates a PaymentIntent
     * 
     * @param userId  User ID making the payment
     * @param request Payment request containing amount, currency, etc.
     * @return PaymentResponse with payment details and client secret
     */
    PaymentResponse processPayment(Long userId, PaymentRequest request);

    /**
     * Confirm a payment.
     * For Stripe: Confirms a PaymentIntent
     * 
     * @param paymentId       Payment ID
     * @param paymentMethodId Optional payment method ID for confirmation
     * @return Updated PaymentResponse
     */
    PaymentResponse confirmPayment(Long paymentId, Long paymentMethodId);

    /**
     * Get payment details by ID.
     * 
     * @param paymentId Payment ID
     * @return PaymentResponse
     */
    PaymentResponse getPayment(Long paymentId);

    /**
     * Get payment by provider payment ID (for webhook processing).
     * 
     * @param providerPaymentId Provider-specific payment ID (e.g., Stripe
     *                          PaymentIntent ID)
     * @return PaymentResponse
     */
    PaymentResponse getPaymentByProviderId(String providerPaymentId);

    /**
     * List payments for a user (paginated).
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of PaymentResponse
     */
    Page<PaymentResponse> listPayments(Long userId, Pageable pageable);

    /**
     * Process a refund for a payment.
     * 
     * @param paymentId Payment ID
     * @param request   Refund request containing amount and reason
     * @return RefundResponse with refund details
     */
    RefundResponse refundPayment(Long paymentId, RefundRequest request);

    /**
     * Get payment status.
     * 
     * @param paymentId Payment ID
     * @return PaymentStatusResponse
     */
    PaymentStatusResponse getPaymentStatus(Long paymentId);

    /**
     * Update payment status from webhook (internal use).
     * 
     * @param providerPaymentId Provider-specific payment ID
     * @param status            New payment status
     */
    void updatePaymentStatus(String providerPaymentId,
            com.hafizbahtiar.spring.features.payment.entity.PaymentStatus status);
}
