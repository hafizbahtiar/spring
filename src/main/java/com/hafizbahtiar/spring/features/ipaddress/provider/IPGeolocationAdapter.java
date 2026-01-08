package com.hafizbahtiar.spring.features.ipaddress.provider;

import com.hafizbahtiar.spring.features.ipaddress.dto.IPGeolocationData;
import com.hafizbahtiar.spring.features.ipaddress.entity.IPGeolocationProvider;

/**
 * Abstraction interface for IP geolocation provider integrations.
 * Implementations: IPLocalizeAdapter, IPLocateAdapter
 * 
 * This interface allows the IPGeolocationService to work with any IP geolocation provider
 * without being tightly coupled to a specific implementation.
 */
public interface IPGeolocationAdapter {

    /**
     * Get the IP geolocation provider this adapter handles
     * 
     * @return IPGeolocationProvider enum value
     */
    IPGeolocationProvider getProvider();

    /**
     * Get geolocation data for an IP address
     * 
     * @param ipAddress IP address to lookup
     * @return Geolocation data or null if lookup fails
     */
    IPGeolocationData getGeolocation(String ipAddress);

    /**
     * Check if this adapter is enabled/configured
     * 
     * @return true if adapter is enabled and ready to use
     */
    boolean isEnabled();
}

