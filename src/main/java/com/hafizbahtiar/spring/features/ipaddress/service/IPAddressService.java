package com.hafizbahtiar.spring.features.ipaddress.service;

import com.hafizbahtiar.spring.features.ipaddress.dto.IPLookupResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for IP address lookup operations.
 * Provides geolocation lookup functionality for owner/admin users.
 */
public interface IPAddressService {

    /**
     * Lookup geolocation for an IP address.
     * 
     * @param ipAddress IP address to lookup
     * @param request   HTTP request for logging context
     * @return IPLookupResponse with geolocation data, or null if lookup fails
     */
    IPLookupResponse lookupIP(String ipAddress, HttpServletRequest request);

    /**
     * Get geolocation data for a session's IP address.
     * 
     * @param sessionId Session ID
     * @param request   HTTP request for logging context
     * @return IPLookupResponse with geolocation data, or null if session not found
     *         or no geolocation data
     */
    IPLookupResponse getSessionIPGeolocation(String sessionId, HttpServletRequest request);
}
