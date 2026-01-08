package com.hafizbahtiar.spring.features.ipaddress.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for IP address lookup.
 * Contains geolocation information for an IP address.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPLookupResponse {

    /**
     * IP address that was looked up
     */
    private String ip;

    /**
     * Full country name (e.g., "United States")
     */
    private String country;

    /**
     * ISO country code (e.g., "US", "MY")
     */
    private String countryCode;

    /**
     * City name (e.g., "Mountain View")
     */
    private String city;

    /**
     * Region/State/Province name (e.g., "California")
     */
    private String region;

    /**
     * Latitude coordinate (decimal degrees)
     */
    private Double latitude;

    /**
     * Longitude coordinate (decimal degrees)
     */
    private Double longitude;

    /**
     * IANA timezone (e.g., "America/New_York")
     */
    private String timezone;

    /**
     * Internet Service Provider name (e.g., "Google LLC")
     */
    private String isp;

    /**
     * Optional: Postal/ZIP code
     */
    private String postalCode;

    /**
     * Optional: Continent name
     */
    private String continent;

    /**
     * Optional: Currency code
     */
    private String currency;

    /**
     * Optional: Calling code
     */
    private String callingCode;
}
