package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.CurrencyPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.CurrencyPreferencesResponse;

/**
 * Service interface for currency preferences management.
 * Handles CRUD operations for user's currency preferences.
 */
public interface CurrencyPreferencesService {

    /**
     * Get currency preferences for a user.
     * Creates default preferences if one doesn't exist.
     *
     * @param userId User ID
     * @return CurrencyPreferencesResponse
     */
    CurrencyPreferencesResponse getCurrencyPreferences(Long userId);

    /**
     * Update currency preferences for a user.
     * Creates preferences if one doesn't exist.
     * Validates currency codes against supported list.
     *
     * @param userId  User ID
     * @param request Update request
     * @return Updated CurrencyPreferencesResponse
     */
    CurrencyPreferencesResponse updateCurrencyPreferences(Long userId, CurrencyPreferencesRequest request);
}
