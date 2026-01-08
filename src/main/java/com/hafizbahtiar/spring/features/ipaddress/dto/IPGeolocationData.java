package com.hafizbahtiar.spring.features.ipaddress.dto;

import lombok.Data;

/**
 * Data Transfer Object for IP geolocation information.
 * Maps to Session entity fields for storing geolocation data.
 */
@Data
public class IPGeolocationData {
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
     * Maps to Session.country field
     */
    private String countryCode;

    /**
     * City name (e.g., "Mountain View")
     * Maps to Session.city field
     */
    private String city;

    /**
     * Region/State/Province name (e.g., "California")
     * Maps to Session.region field
     */
    private String region;

    /**
     * Latitude coordinate (decimal degrees)
     * Maps to Session.latitude field
     */
    private Double latitude;

    /**
     * Longitude coordinate (decimal degrees)
     * Maps to Session.longitude field
     */
    private Double longitude;

    /**
     * IANA timezone (e.g., "America/New_York", "Asia/Kuala_Lumpur")
     * Maps to Session.timezone field
     */
    private String timezone;

    /**
     * Internet Service Provider name (e.g., "Google LLC")
     * Maps to Session.isp field
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
