package com.hafizbahtiar.spring.features.payment.service;

import com.hafizbahtiar.spring.common.exception.ProviderException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentMethodException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentMethodNotFoundException;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.payment.dto.PaymentMethodRequest;
import com.hafizbahtiar.spring.features.payment.dto.PaymentMethodResponse;
import com.hafizbahtiar.spring.features.payment.entity.PaymentMethod;
import com.hafizbahtiar.spring.features.payment.entity.PaymentMethodType;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.mapper.PaymentMethodMapper;
import com.hafizbahtiar.spring.features.payment.provider.PaymentProviderService;
import com.hafizbahtiar.spring.features.payment.repository.PaymentMethodRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PaymentMethodService.
 * Handles payment method management using PaymentProviderService abstraction.
 * Currently supports Stripe (to be implemented), extensible for PayPal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;
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
    public PaymentMethodResponse addPaymentMethod(Long userId, PaymentMethodRequest request) {
        log.debug("Adding payment method for user ID: {}, provider: {}", userId, request.getProvider());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Check if payment method already exists
        if (paymentMethodRepository.existsByProviderMethodId(request.getProviderMethodId())) {
            throw PaymentMethodException.alreadyExists(request.getProviderMethodId());
        }

        // Get provider service
        PaymentProviderService providerService = getProviderService(request.getProvider());

        // Get or create customer ID
        String customerId = request.getProviderCustomerId();
        if (customerId == null) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", userId.toString());
            customerId = providerService.createOrRetrieveCustomer(user.getEmail(), metadata);
        }

        // Attach payment method to customer (for Stripe)
        try {
            providerService.attachPaymentMethodToCustomer(customerId, request.getProviderMethodId());
        } catch (Exception e) {
            log.error("Failed to attach payment method to customer: {}", e.getMessage(), e);
            throw PaymentMethodException.attachmentFailed(e.getMessage());
        }

        // Create payment method entity
        PaymentMethod paymentMethod = new PaymentMethod(user, request.getProvider(), null); // Type will be determined
                                                                                            // from provider
        paymentMethod.setProviderMethodId(request.getProviderMethodId());
        paymentMethod.setProviderCustomerId(customerId);

        // Set as default if requested
        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            // Unset other defaults for this user and provider
            paymentMethodRepository.unsetAllDefaultsByUserIdAndProvider(userId, request.getProvider());
            paymentMethod.setAsDefault();
        }

        // Fetch payment method details from provider (card details, etc.)
        try {
            PaymentProviderService.ProviderPaymentMethodDetails details = providerService
                    .getPaymentMethodDetails(request.getProviderMethodId());

            // Map provider type to our PaymentMethodType enum
            if (details.getType() != null) {
                PaymentMethodType methodType = mapProviderTypeToPaymentMethodType(details.getType());
                paymentMethod.setType(methodType);
            }

            // Set card details if available
            if (details.getLast4() != null) {
                paymentMethod.setLast4(details.getLast4());
            }
            if (details.getBrand() != null) {
                paymentMethod.setBrand(details.getBrand());
            }
            if (details.getExpiryMonth() != null) {
                paymentMethod.setExpiryMonth(details.getExpiryMonth());
            }
            if (details.getExpiryYear() != null) {
                paymentMethod.setExpiryYear(details.getExpiryYear());
            }
            if (details.getMetadata() != null) {
                paymentMethod.setMetadata(details.getMetadata());
            }

            log.debug("Fetched payment method details from provider: type={}, brand={}, last4={}",
                    details.getType(), details.getBrand(), details.getLast4());

        } catch (Exception e) {
            log.warn("Failed to fetch payment method details from provider: {}. " +
                    "Payment method will be saved with basic information. Error: {}",
                    e.getMessage(), e);
            // Continue with save even if details fetch fails
            // Set a default type if not set
            if (paymentMethod.getType() == null) {
                paymentMethod.setType(PaymentMethodType.STRIPE_PAYMENT_METHOD);
            }
        }

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        log.info("Payment method added successfully with ID: {}, provider method ID: {}",
                savedPaymentMethod.getId(), savedPaymentMethod.getProviderMethodId());

        // Log payment method addition
        paymentLoggingService.logPaymentMethodAdded(
                savedPaymentMethod.getId(),
                userId,
                savedPaymentMethod.getProvider().name(),
                savedPaymentMethod.getProviderMethodId(),
                getCurrentRequest());

        return paymentMethodMapper.toResponse(savedPaymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> listPaymentMethods(Long userId) {
        log.debug("Listing payment methods for user ID: {}", userId);
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return paymentMethodMapper.toResponseList(paymentMethods);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse getPaymentMethod(Long paymentMethodId) {
        log.debug("Fetching payment method with ID: {}", paymentMethodId);
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> PaymentMethodNotFoundException.byId(paymentMethodId));
        return paymentMethodMapper.toResponse(paymentMethod);
    }

    @Override
    public PaymentMethodResponse setDefaultPaymentMethod(Long userId, Long paymentMethodId) {
        log.debug("Setting default payment method for user ID: {}, payment method ID: {}", userId, paymentMethodId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> PaymentMethodNotFoundException.byId(paymentMethodId));

        // Verify payment method belongs to user
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw PaymentMethodException.notOwnedByUser();
        }

        // Unset other defaults for this user and provider
        paymentMethodRepository.unsetAllDefaultsByUserIdAndProvider(userId, paymentMethod.getProvider());

        // Set as default
        paymentMethod.setAsDefault();
        PaymentMethod updatedPaymentMethod = paymentMethodRepository.save(paymentMethod);

        log.info("Default payment method set successfully for user ID: {}, payment method ID: {}",
                userId, paymentMethodId);

        return paymentMethodMapper.toResponse(updatedPaymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse getDefaultPaymentMethod(Long userId) {
        log.debug("Fetching default payment method for user ID: {}", userId);
        PaymentMethod paymentMethod = paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElse(null);
        return paymentMethod != null ? paymentMethodMapper.toResponse(paymentMethod) : null;
    }

    @Override
    public void removePaymentMethod(Long userId, Long paymentMethodId) {
        log.debug("Removing payment method for user ID: {}, payment method ID: {}", userId, paymentMethodId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> PaymentMethodNotFoundException.byId(paymentMethodId));

        // Verify payment method belongs to user
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw PaymentMethodException.notOwnedByUser();
        }

        // Detach payment method from provider customer (for Stripe)
        if (paymentMethod.getProviderCustomerId() != null && paymentMethod.getProviderMethodId() != null) {
            PaymentProviderService providerService = getProviderService(paymentMethod.getProvider());
            try {
                providerService.detachPaymentMethodFromCustomer(
                        paymentMethod.getProviderCustomerId(),
                        paymentMethod.getProviderMethodId());
                log.debug("Payment method detached from provider customer successfully");
            } catch (Exception e) {
                log.error("Failed to detach payment method from provider customer: {}. " +
                        "Payment method will still be removed from database. Error: {}",
                        e.getMessage(), e);
                // Continue with deletion even if provider detachment fails
                // This ensures data consistency - if provider call fails, we still remove from
                // DB
            }
        }

        // Delete payment method
        paymentMethodRepository.delete(paymentMethod);
        log.info("Payment method removed successfully for user ID: {}, payment method ID: {}",
                userId, paymentMethodId);

        // Log payment method removal
        paymentLoggingService.logPaymentMethodRemoved(
                paymentMethodId,
                userId,
                paymentMethod.getProvider().name(),
                getCurrentRequest());
    }

    /**
     * Map provider payment method type to our PaymentMethodType enum
     */
    private PaymentMethodType mapProviderTypeToPaymentMethodType(String providerType) {
        if (providerType == null) {
            return PaymentMethodType.STRIPE_PAYMENT_METHOD;
        }

        return switch (providerType.toLowerCase()) {
            case "card" -> PaymentMethodType.CREDIT_CARD; // Default to credit card for card type
            case "paypal" -> PaymentMethodType.PAYPAL_ACCOUNT;
            case "us_bank_account", "sepa_debit", "ach_debit" -> PaymentMethodType.BANK_TRANSFER;
            default -> PaymentMethodType.STRIPE_PAYMENT_METHOD;
        };
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
