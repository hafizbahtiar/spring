package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.CurrencyPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.CurrencyPreferencesResponse;
import com.hafizbahtiar.spring.features.user.entity.CurrencyPreferences;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.mapper.CurrencyPreferencesMapper;
import com.hafizbahtiar.spring.features.user.repository.CurrencyPreferencesRepository;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of CurrencyPreferencesService.
 * Handles currency preferences CRUD operations with automatic preferences
 * creation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CurrencyPreferencesServiceImpl implements CurrencyPreferencesService {

    private final CurrencyPreferencesRepository currencyPreferencesRepository;
    private final CurrencyPreferencesMapper currencyPreferencesMapper;
    private final UserRepository userRepository;
    private final UserActivityLoggingService userActivityLoggingService;

    /**
     * Supported currency codes (ISO 4217)
     * Common currencies used in the application
     */
    private static final Set<String> SUPPORTED_CURRENCY_CODES = Set.of(
            "MYR", "USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF", "INR",
            "SGD", "HKD", "KRW", "THB", "IDR", "PHP", "VND", "BRL", "MXN", "ZAR",
            "NZD", "SEK", "NOK", "DKK", "PLN", "CZK", "HUF", "RUB", "TRY", "AED");

    /**
     * Get current HttpServletRequest for logging context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("Could not get current request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate currency code against supported list
     */
    private void validateCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }
        String upperCurrency = currencyCode.toUpperCase().trim();
        if (!SUPPORTED_CURRENCY_CODES.contains(upperCurrency)) {
            throw new IllegalArgumentException(
                    String.format("Currency code '%s' is not supported. Supported currencies: %s",
                            currencyCode, String.join(", ", SUPPORTED_CURRENCY_CODES)));
        }
    }

    /**
     * Validate list of currency codes
     */
    private void validateCurrencyCodes(List<String> currencyCodes) {
        if (currencyCodes == null || currencyCodes.isEmpty()) {
            throw new IllegalArgumentException("Supported currencies list cannot be null or empty");
        }
        Set<String> uniqueCurrencies = new HashSet<>();
        for (String currency : currencyCodes) {
            if (currency == null || currency.trim().isEmpty()) {
                throw new IllegalArgumentException("Currency code cannot be null or empty");
            }
            String upperCurrency = currency.toUpperCase().trim();
            validateCurrencyCode(upperCurrency);
            if (!uniqueCurrencies.add(upperCurrency)) {
                throw new IllegalArgumentException(
                        String.format("Duplicate currency code found: %s", currency));
            }
        }
    }

    @Override
    @Transactional
    public CurrencyPreferencesResponse getCurrencyPreferences(Long userId) {
        log.debug("Fetching currency preferences for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Get or create preferences
        // Note: Cannot use readOnly=true because we may need to create preferences
        CurrencyPreferences preferences = currencyPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating default currency preferences for user ID: {}", userId);
                    CurrencyPreferences newPreferences = new CurrencyPreferences(user);
                    return currencyPreferencesRepository.save(newPreferences);
                });

        return currencyPreferencesMapper.toResponse(preferences);
    }

    @Override
    public CurrencyPreferencesResponse updateCurrencyPreferences(Long userId, CurrencyPreferencesRequest request) {
        log.debug("Updating currency preferences for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate currency codes if provided
        if (request.getBaseCurrency() != null) {
            validateCurrencyCode(request.getBaseCurrency());
        }
        if (request.getSupportedCurrencies() != null) {
            validateCurrencyCodes(request.getSupportedCurrencies());
            // Ensure base currency is in supported currencies if both are provided
            if (request.getBaseCurrency() != null) {
                String baseCurrency = request.getBaseCurrency().toUpperCase().trim();
                List<String> supportedCurrencies = request.getSupportedCurrencies().stream()
                        .map(c -> c.toUpperCase().trim())
                        .toList();
                if (!supportedCurrencies.contains(baseCurrency)) {
                    // Add base currency to supported currencies if not present
                    request.getSupportedCurrencies().add(baseCurrency);
                    log.info("Added base currency '{}' to supported currencies list", baseCurrency);
                }
            }
        }

        // Get or create preferences
        CurrencyPreferences preferences = currencyPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new currency preferences for user ID: {}", userId);
                    CurrencyPreferences newPreferences = new CurrencyPreferences(user);
                    return currencyPreferencesRepository.save(newPreferences);
                });

        // Normalize currency codes in request before mapping
        if (request.getBaseCurrency() != null) {
            request.setBaseCurrency(request.getBaseCurrency().toUpperCase().trim());
        }
        if (request.getSupportedCurrencies() != null) {
            List<String> normalizedCurrencies = request.getSupportedCurrencies().stream()
                    .map(c -> c.toUpperCase().trim())
                    .distinct()
                    .toList();
            request.setSupportedCurrencies(normalizedCurrencies);
        }

        // Update preferences from request using mapper (only non-null fields)
        currencyPreferencesMapper.updateEntityFromRequest(request, preferences);

        // Ensure base currency is always in supported currencies
        String baseCurrency = preferences.getBaseCurrency();
        List<String> supportedCurrencies = (List<String>) preferences.getSupportedCurrencies();
        if (supportedCurrencies == null) {
            supportedCurrencies = new java.util.ArrayList<>();
            preferences.setSupportedCurrencies(supportedCurrencies);
        }
        if (!supportedCurrencies.contains(baseCurrency)) {
            supportedCurrencies = new java.util.ArrayList<>(supportedCurrencies);
            supportedCurrencies.add(baseCurrency);
            preferences.setSupportedCurrencies(supportedCurrencies);
        }

        long startTime = System.currentTimeMillis();
        CurrencyPreferences updatedPreferences = currencyPreferencesRepository.save(preferences);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Currency preferences updated successfully for user ID: {}", userId);

        // Log preferences update
        userActivityLoggingService.logCurrencyPreferencesUpdated(
                updatedPreferences.getId(),
                userId,
                getCurrentRequest(),
                responseTime);

        return currencyPreferencesMapper.toResponse(updatedPreferences);
    }
}
