package com.hafizbahtiar.spring.features.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating currency preferences.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyPreferencesRequest {

    /**
     * Base currency (e.g., "MYR", "USD", "EUR")
     * Must be a valid 3-letter ISO currency code
     */
    @Pattern(regexp = "^[A-Z]{3}$", message = "Base currency must be a valid 3-letter ISO currency code (e.g., MYR, USD, EUR)")
    @Size(min = 3, max = 10, message = "Base currency must be between 3 and 10 characters")
    private String baseCurrency;

    /**
     * Supported currencies list
     * List of currency codes the user wants to work with
     */
    private List<@Pattern(regexp = "^[A-Z]{3}$", message = "Each currency code must be a valid 3-letter ISO currency code") String> supportedCurrencies;
}
