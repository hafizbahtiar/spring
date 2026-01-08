package com.hafizbahtiar.spring.features.payment.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.payment.dto.PaymentRequest;
import com.hafizbahtiar.spring.features.payment.dto.PaymentResponse;
import com.hafizbahtiar.spring.features.payment.dto.PaymentStatusResponse;
import com.hafizbahtiar.spring.features.payment.dto.RefundRequest;
import com.hafizbahtiar.spring.features.payment.dto.RefundResponse;
import com.hafizbahtiar.spring.features.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hafizbahtiar.spring.common.security.UserPrincipal;

/**
 * REST controller for payment management endpoints.
 * Handles payment processing, confirmation, refunds, and status checks.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
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
     * Process a payment (create payment intent)
     * POST /api/v1/payments
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        Long userId = getCurrentUserId();
        log.info("Payment processing request received for user ID: {}, amount: {} {}",
                userId, request.getAmount(), request.getCurrency());
        PaymentResponse response = paymentService.processPayment(userId, request);
        return ResponseUtils.created(response, "Payment intent created successfully");
    }

    /**
     * Confirm a payment
     * POST /api/v1/payments/{id}/confirm
     * Requires: User owns the payment OR ADMIN role
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @PathVariable Long id,
            @RequestParam(required = false) Long paymentMethodId) {
        log.info("Payment confirmation request received for payment ID: {}", id);

        // Verify ownership
        PaymentResponse payment = paymentService.getPayment(id);
        Long userId = getCurrentUserId();
        if (!payment.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only confirm your own payments");
        }

        PaymentResponse response = paymentService.confirmPayment(id, paymentMethodId);
        return ResponseUtils.ok(response, "Payment confirmed successfully");
    }

    /**
     * Get payment details by ID
     * GET /api/v1/payments/{id}
     * Requires: User owns the payment OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long id) {
        log.debug("Fetching payment with ID: {}", id);

        PaymentResponse payment = paymentService.getPayment(id);
        Long userId = getCurrentUserId();
        if (!payment.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own payments");
        }

        return ResponseUtils.ok(payment);
    }

    /**
     * List payments for current user (paginated)
     * GET /api/v1/payments
     * Requires: Authenticated user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginatedResponse<PaymentResponse>>> listPayments(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Listing payments for user ID: {}", userId);
        Page<PaymentResponse> payments = paymentService.listPayments(userId, pageable);
        return ResponseUtils.okPage(payments);
    }

    /**
     * Process a refund for a payment
     * POST /api/v1/payments/{id}/refund
     * Requires: User owns the payment OR ADMIN role
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RefundResponse>> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        log.info("Refund request received for payment ID: {}", id);

        // Verify ownership
        PaymentResponse payment = paymentService.getPayment(id);
        Long userId = getCurrentUserId();
        if (!payment.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only refund your own payments");
        }

        RefundResponse response = paymentService.refundPayment(id, request);
        return ResponseUtils.ok(response, "Refund processed successfully");
    }

    /**
     * Get payment status
     * GET /api/v1/payments/{id}/status
     * Requires: User owns the payment OR ADMIN role
     */
    @GetMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(@PathVariable Long id) {
        log.debug("Fetching payment status for ID: {}", id);

        PaymentResponse payment = paymentService.getPayment(id);
        Long userId = getCurrentUserId();
        if (!payment.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own payment status");
        }

        PaymentStatusResponse response = paymentService.getPaymentStatus(id);
        return ResponseUtils.ok(response);
    }
}
