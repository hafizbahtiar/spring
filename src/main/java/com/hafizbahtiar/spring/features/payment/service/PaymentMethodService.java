package com.hafizbahtiar.spring.features.payment.service;

import com.hafizbahtiar.spring.features.payment.dto.PaymentMethodRequest;
import com.hafizbahtiar.spring.features.payment.dto.PaymentMethodResponse;

import java.util.List;

/**
 * Service interface for payment method management.
 * Handles adding, listing, updating, and removing payment methods.
 */
public interface PaymentMethodService {

    /**
     * Add a payment method for a user.
     * For Stripe: Attaches PaymentMethod to Customer
     * 
     * @param userId  User ID
     * @param request Payment method request containing provider method ID
     * @return PaymentMethodResponse with saved payment method details
     */
    PaymentMethodResponse addPaymentMethod(Long userId, PaymentMethodRequest request);

    /**
     * List all payment methods for a user.
     * 
     * @param userId User ID
     * @return List of PaymentMethodResponse
     */
    List<PaymentMethodResponse> listPaymentMethods(Long userId);

    /**
     * Get payment method by ID.
     * 
     * @param paymentMethodId Payment method ID
     * @return PaymentMethodResponse
     */
    PaymentMethodResponse getPaymentMethod(Long paymentMethodId);

    /**
     * Set a payment method as default for a user.
     * 
     * @param userId          User ID
     * @param paymentMethodId Payment method ID to set as default
     * @return Updated PaymentMethodResponse
     */
    PaymentMethodResponse setDefaultPaymentMethod(Long userId, Long paymentMethodId);

    /**
     * Get default payment method for a user.
     * 
     * @param userId User ID
     * @return PaymentMethodResponse or null if no default method
     */
    PaymentMethodResponse getDefaultPaymentMethod(Long userId);

    /**
     * Remove a payment method.
     * 
     * @param userId          User ID
     * @param paymentMethodId Payment method ID to remove
     */
    void removePaymentMethod(Long userId, Long paymentMethodId);
}
