package com.hafizbahtiar.spring.features.payment.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.payment.dto.PaymentMethodRequest;
import com.hafizbahtiar.spring.features.payment.dto.PaymentMethodResponse;
import com.hafizbahtiar.spring.features.payment.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hafizbahtiar.spring.common.security.UserPrincipal;

import java.util.List;

/**
 * REST controller for payment method management endpoints.
 * Handles adding, listing, updating, and removing payment methods.
 */
@RestController
@RequestMapping("/api/v1/payments/methods")
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
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
     * Add a payment method for current user
     * POST /api/v1/payments/methods
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> addPaymentMethod(
            @Valid @RequestBody PaymentMethodRequest request) {
        Long userId = getCurrentUserId();
        log.info("Add payment method request received for user ID: {}, provider: {}",
                userId, request.getProvider());
        PaymentMethodResponse response = paymentMethodService.addPaymentMethod(userId, request);
        return ResponseUtils.created(response, "Payment method added successfully");
    }

    /**
     * List all payment methods for current user
     * GET /api/v1/payments/methods
     * Requires: Authenticated user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> listPaymentMethods() {
        Long userId = getCurrentUserId();
        log.debug("Listing payment methods for user ID: {}", userId);
        List<PaymentMethodResponse> paymentMethods = paymentMethodService.listPaymentMethods(userId);
        return ResponseUtils.ok(paymentMethods);
    }

    /**
     * Get payment method by ID
     * GET /api/v1/payments/methods/{id}
     * Requires: User owns the payment method OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getPaymentMethod(@PathVariable Long id) {
        log.debug("Fetching payment method with ID: {}", id);

        PaymentMethodResponse paymentMethod = paymentMethodService.getPaymentMethod(id);
        Long userId = getCurrentUserId();
        if (!paymentMethod.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own payment methods");
        }

        return ResponseUtils.ok(paymentMethod);
    }

    /**
     * Set a payment method as default for current user
     * PUT /api/v1/payments/methods/{id}/default
     * Requires: User owns the payment method OR ADMIN role
     */
    @PutMapping("/{id}/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> setDefaultPaymentMethod(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Set default payment method request for user ID: {}, payment method ID: {}",
                userId, id);

        // Verify ownership
        PaymentMethodResponse paymentMethod = paymentMethodService.getPaymentMethod(id);
        if (!paymentMethod.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only set your own payment methods as default");
        }

        PaymentMethodResponse response = paymentMethodService.setDefaultPaymentMethod(userId, id);
        return ResponseUtils.ok(response, "Default payment method updated successfully");
    }

    /**
     * Get default payment method for current user
     * GET /api/v1/payments/methods/default
     * Requires: Authenticated user
     */
    @GetMapping("/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getDefaultPaymentMethod() {
        Long userId = getCurrentUserId();
        log.debug("Fetching default payment method for user ID: {}", userId);
        PaymentMethodResponse paymentMethod = paymentMethodService.getDefaultPaymentMethod(userId);

        if (paymentMethod == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseUtils.ok(paymentMethod);
    }

    /**
     * Remove a payment method
     * DELETE /api/v1/payments/methods/{id}
     * Requires: User owns the payment method OR ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removePaymentMethod(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Remove payment method request for user ID: {}, payment method ID: {}",
                userId, id);

        // Verify ownership
        PaymentMethodResponse paymentMethod = paymentMethodService.getPaymentMethod(id);
        if (!paymentMethod.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only remove your own payment methods");
        }

        paymentMethodService.removePaymentMethod(userId, id);
        return ResponseUtils.noContent();
    }
}
