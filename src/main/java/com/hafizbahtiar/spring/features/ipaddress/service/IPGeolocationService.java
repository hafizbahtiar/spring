package com.hafizbahtiar.spring.features.ipaddress.service;

import com.hafizbahtiar.spring.features.ipaddress.dto.IPGeolocationData;
import com.hafizbahtiar.spring.features.ipaddress.entity.IPGeolocationProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for IP geolocation operations.
 * Provides geolocation data for IP addresses using configured providers.
 */
public interface IPGeolocationService {

    /**
     * Get geolocation data for an IP address using the configured primary provider.
     * 
     * @param ipAddress IP address to lookup
     * @return Geolocation data or null if lookup fails
     */
    IPGeolocationData getGeolocation(String ipAddress);

    /**
     * Get geolocation data asynchronously.
     * 
     * @param ipAddress IP address to lookup
     * @return CompletableFuture with geolocation data
     */
    CompletableFuture<IPGeolocationData> getGeolocationAsync(String ipAddress);

    /**
     * Get geolocation data from a specific provider.
     * 
     * @param ipAddress IP address to lookup
     * @param provider Specific provider to use
     * @return Geolocation data or null if lookup fails
     */
    IPGeolocationData getGeolocation(String ipAddress, IPGeolocationProvider provider);
}

