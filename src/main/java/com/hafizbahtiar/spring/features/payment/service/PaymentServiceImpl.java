package com.hafizbahtiar.spring.features.payment.service;

import com.hafizbahtiar.spring.common.exception.ProviderException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentMethodException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentMethodNotFoundException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentNotFoundException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentProcessingException;
import com.hafizbahtiar.spring.features.payment.exception.RefundException;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.payment.dto.PaymentRequest;
import com.hafizbahtiar.spring.features.payment.dto.PaymentResponse;
import com.hafizbahtiar.spring.features.payment.dto.PaymentStatusResponse;
import com.hafizbahtiar.spring.features.payment.dto.RefundRequest;
import com.hafizbahtiar.spring.features.payment.dto.RefundResponse;
import com.hafizbahtiar.spring.features.payment.entity.Payment;
import com.hafizbahtiar.spring.features.payment.entity.PaymentMethod;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.entity.PaymentStatus;
import com.hafizbahtiar.spring.features.payment.mapper.PaymentMapper;
import com.hafizbahtiar.spring.features.payment.provider.PaymentProviderService;
import com.hafizbahtiar.spring.features.payment.repository.PaymentMethodRepository;
import com.hafizbahtiar.spring.features.payment.repository.PaymentRepository;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PaymentService.
 * Handles payment processing using PaymentProviderService abstraction.
 * Currently supports Stripe (to be implemented), extensible for PayPal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final List<PaymentProviderService> paymentProviderServices; // Injected list of all providers
    private final PaymentLoggingService paymentLoggingService;

    /**
     * Get the appropriate payment provider service for a given provider
     */
    private PaymentProviderService getProviderService(PaymentProvider provider) {
        return paymentProviderServices.stream()
                .filter(service -> service.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> ProviderException.notSupported(provider.name()));
    }

    @Override
    public PaymentResponse processPayment(Long userId, PaymentRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("Processing payment for user ID: {}, amount: {} {}", userId, request.getAmount(),
                request.getCurrency());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Determine provider (default to STRIPE for now)
        PaymentProvider provider = PaymentProvider.STRIPE; // TODO: Allow provider selection in request

        // Get provider service
        PaymentProviderService providerService = getProviderService(provider);

        // Prepare metadata
        Map<String, String> metadata = new HashMap<>();
        if (request.getDescription() != null) {
            metadata.put("description", request.getDescription());
        }
        if (request.getOrderId() != null) {
            metadata.put("orderId", request.getOrderId().toString());
        }
        if (request.getSubscriptionId() != null) {
            metadata.put("subscriptionId", request.getSubscriptionId().toString());
        }

        // Get or create customer ID
        String customerId = null;
        if (request.getSavePaymentMethod() || request.getPaymentMethodId() != null) {
            customerId = providerService.createOrRetrieveCustomer(user.getEmail(), metadata);
        }

        // Get payment method ID if provided
        String paymentMethodId = null;
        if (request.getPaymentMethodId() != null) {
            // Fetch payment method and validate ownership
            PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> PaymentMethodNotFoundException.byId(request.getPaymentMethodId()));

            // Validate payment method belongs to the user
            if (!paymentMethod.getUser().getId().equals(userId)) {
                throw PaymentMethodException.notOwnedByUser();
            }

            // Validate payment method provider matches the selected provider
            if (paymentMethod.getProvider() != provider) {
                throw PaymentMethodException.invalid(
                        "Payment method provider (" + paymentMethod.getProvider()
                                + ") does not match selected provider (" + provider + ")");
            }

            // Get provider-specific payment method ID
            paymentMethodId = paymentMethod.getProviderMethodId();
            if (paymentMethodId == null || paymentMethodId.isEmpty()) {
                throw PaymentMethodException.invalid("Payment method does not have a provider method ID");
            }

            // Ensure customer ID is set (needed for payment method)
            if (customerId == null) {
                customerId = providerService.createOrRetrieveCustomer(user.getEmail(), metadata);
            }
        }

        // Create payment with provider
        PaymentProviderService.ProviderPaymentResult providerResult;
        try {
            providerResult = providerService.createPayment(
                    request.getAmount(),
                    request.getCurrency(),
                    customerId,
                    paymentMethodId,
                    metadata);
        } catch (Exception e) {
            log.error("Failed to create payment with provider: {}", e.getMessage(), e);
            throw PaymentProcessingException.failed(e.getMessage());
        }

        // Create payment entity
        Payment payment = new Payment(user, provider, request.getAmount(), request.getCurrency());
        payment.setProviderPaymentId(providerResult.getProviderPaymentId());
        payment.setClientSecret(providerResult.getClientSecret());
        payment.setStatus(providerResult.getStatus());
        payment.setOrderId(request.getOrderId());
        payment.setSubscriptionId(request.getSubscriptionId());
        payment.setPaymentMethodId(request.getPaymentMethodId());

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Payment created successfully with ID: {}, provider payment ID: {}",
                savedPayment.getId(), savedPayment.getProviderPaymentId());

        // Log payment creation
        paymentLoggingService.logPaymentCreated(
                savedPayment.getId(),
                userId,
                savedPayment.getProvider().name(),
                savedPayment.getProviderPaymentId(),
                savedPayment.getAmount(),
                savedPayment.getCurrency(),
                savedPayment.getStatus().name(),
                getCurrentRequest(),
                responseTime);

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse confirmPayment(Long paymentId, Long paymentMethodId) {
        long startTime = System.currentTimeMillis();
        log.debug("Confirming payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.byId(paymentId));

        // Check if payment can be confirmed
        if (payment.isTerminal()) {
            throw PaymentProcessingException.confirmationFailed(
                    "Payment is in terminal state: " + payment.getStatus());
        }

        // Get provider service
        PaymentProviderService providerService = getProviderService(payment.getProvider());

        // Get payment method ID if provided
        String providerPaymentMethodId = null;
        if (paymentMethodId != null) {
            // Fetch payment method and validate ownership
            PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                    .orElseThrow(() -> PaymentMethodNotFoundException.byId(paymentMethodId));

            // Validate payment method belongs to the user who owns the payment
            if (!paymentMethod.getUser().getId().equals(payment.getUser().getId())) {
                throw PaymentMethodException.notOwnedByUser();
            }

            // Validate payment method provider matches the payment provider
            if (paymentMethod.getProvider() != payment.getProvider()) {
                throw PaymentMethodException.invalid(
                        "Payment method provider (" + paymentMethod.getProvider()
                                + ") does not match payment provider (" + payment.getProvider() + ")");
            }

            // Get provider-specific payment method ID
            providerPaymentMethodId = paymentMethod.getProviderMethodId();
            if (providerPaymentMethodId == null || providerPaymentMethodId.isEmpty()) {
                throw PaymentMethodException.invalid("Payment method does not have a provider method ID");
            }
        }

        // Confirm payment with provider
        PaymentProviderService.ProviderPaymentResult providerResult;
        try {
            providerResult = providerService.confirmPayment(
                    payment.getProviderPaymentId(),
                    providerPaymentMethodId);
        } catch (Exception e) {
            log.error("Failed to confirm payment with provider: {}", e.getMessage(), e);
            throw PaymentProcessingException.confirmationFailed(e.getMessage());
        }

        // Update payment status
        payment.setStatus(providerResult.getStatus());
        if (providerResult.getStatus() == PaymentStatus.COMPLETED) {
            payment.markAsCompleted();
        } else if (providerResult.getStatus() == PaymentStatus.FAILED) {
            // Extract failure reason from provider result metadata if available
            payment.markAsFailed("Payment confirmation failed", null);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Payment confirmed successfully with ID: {}, status: {}",
                updatedPayment.getId(), updatedPayment.getStatus());

        // Log payment confirmation
        paymentLoggingService.logPaymentConfirmed(
                updatedPayment.getId(),
                updatedPayment.getUser().getId(),
                updatedPayment.getProvider().name(),
                updatedPayment.getProviderPaymentId(),
                updatedPayment.getStatus().name(),
                getCurrentRequest(),
                responseTime);

        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        log.debug("Fetching payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.byId(paymentId));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByProviderId(String providerPaymentId) {
        log.debug("Fetching payment with provider ID: {}", providerPaymentId);
        Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> PaymentNotFoundException.byProviderId(providerPaymentId));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> listPayments(Long userId, Pageable pageable) {
        log.debug("Listing payments for user ID: {}", userId);
        Page<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return paymentMapper.toResponsePage(payments);
    }

    @Override
    public RefundResponse refundPayment(Long paymentId, RefundRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.byId(paymentId));

        // Validate payment can be refunded
        if (!payment.canBeRefunded()) {
            throw RefundException.notRefundable(
                    "Payment status is " + payment.getStatus() + ". Only completed payments can be refunded.");
        }

        // Determine refund amount
        BigDecimal refundAmount = request.getAmount();
        if (refundAmount == null) {
            // Full refund
            refundAmount = payment.getAmount();
        } else {
            // Partial refund - validate amount
            BigDecimal availableAmount = payment.getAmount();
            if (payment.getRefundAmount() != null) {
                availableAmount = availableAmount.subtract(payment.getRefundAmount());
            }
            if (refundAmount.compareTo(availableAmount) > 0) {
                throw RefundException.invalidAmount(refundAmount, availableAmount);
            }
        }

        // Get provider service
        PaymentProviderService providerService = getProviderService(payment.getProvider());

        // Process refund with provider
        PaymentProviderService.ProviderRefundResult refundResult;
        try {
            refundResult = providerService.refundPayment(
                    payment.getProviderPaymentId(),
                    refundAmount,
                    request.getReason());
        } catch (Exception e) {
            log.error("Failed to process refund with provider: {}", e.getMessage(), e);
            throw RefundException.processingFailed(e.getMessage());
        }

        // Update payment
        payment.markAsRefunded(refundResult.getRefundAmount());
        Payment updatedPayment = paymentRepository.save(payment);
        long responseTime = System.currentTimeMillis() - startTime;

        log.info("Refund processed successfully for payment ID: {}, refund amount: {}",
                updatedPayment.getId(), refundResult.getRefundAmount());

        // Log refund
        paymentLoggingService.logRefund(
                updatedPayment.getId(),
                updatedPayment.getUser().getId(),
                updatedPayment.getProvider().name(),
                updatedPayment.getProviderPaymentId(),
                refundResult.getProviderRefundId(),
                refundResult.getRefundAmount(),
                getCurrentRequest(),
                responseTime);

        return RefundResponse.builder()
                .paymentId(updatedPayment.getId())
                .providerRefundId(refundResult.getProviderRefundId())
                .refundAmount(refundResult.getRefundAmount())
                .paymentStatus(updatedPayment.getStatus())
                .reason(request.getReason())
                .refundedAt(updatedPayment.getRefundedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        log.debug("Fetching payment status for ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.byId(paymentId));
        return paymentMapper.toStatusResponse(payment);
    }

    @Override
    public void updatePaymentStatus(String providerPaymentId, PaymentStatus status) {
        log.debug("Updating payment status for provider payment ID: {}, status: {}", providerPaymentId, status);
        Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> PaymentNotFoundException.byProviderId(providerPaymentId));

        payment.setStatus(status);
        if (status == PaymentStatus.COMPLETED) {
            payment.markAsCompleted();
        }

        paymentRepository.save(payment);
        log.info("Payment status updated successfully for provider payment ID: {}, status: {}",
                providerPaymentId, status);
    }

    /**
     * Get current HTTP request (for logging)
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not get current request: {}", e.getMessage());
            return null;
        }
    }
}
