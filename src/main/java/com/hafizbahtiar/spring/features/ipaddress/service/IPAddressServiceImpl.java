package com.hafizbahtiar.spring.features.ipaddress.service;

import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.features.auth.entity.Session;
import com.hafizbahtiar.spring.features.auth.exception.SessionNotFoundException;
import com.hafizbahtiar.spring.features.auth.repository.SessionRepository;
import com.hafizbahtiar.spring.features.ipaddress.dto.IPGeolocationData;
import com.hafizbahtiar.spring.features.ipaddress.dto.IPLookupResponse;
import com.hafizbahtiar.spring.features.ipaddress.exception.IPAddressException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of IPAddressService.
 * Provides IP geolocation lookup functionality for owner/admin users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IPAddressServiceImpl implements IPAddressService {

    private final IPGeolocationService ipGeolocationService;
    private final SessionRepository sessionRepository;
    private final IPAddressLoggingService ipAddressLoggingService;

    @Override
    public IPLookupResponse lookupIP(String ipAddress, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("Looking up geolocation for IP: {}", ipAddress);

        Long userId = getCurrentUserId();

        if (ipAddress == null || ipAddress.isBlank()) {
            long responseTime = System.currentTimeMillis() - startTime;
            ipAddressLoggingService.logIPLookupFailure(userId, ipAddress, "INVALID_IP", responseTime, request);
            throw IPAddressException.invalidIP(ipAddress);
        }

        IPGeolocationData geoData = ipGeolocationService.getGeolocation(ipAddress);
        long responseTime = System.currentTimeMillis() - startTime;

        if (geoData == null) {
            log.warn("No geolocation data found for IP: {}", ipAddress);
            ipAddressLoggingService.logIPLookupFailure(userId, ipAddress, "IP_NOT_FOUND", responseTime, request);
            return null;
        }

        IPLookupResponse response = mapToIPLookupResponse(geoData);
        ipAddressLoggingService.logIPLookupSuccess(userId, ipAddress, response, responseTime, request);
        return response;
    }

    @Override
    public IPLookupResponse getSessionIPGeolocation(String sessionId, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        log.debug("Getting geolocation for session IP: {}", sessionId);

        Long userId = getCurrentUserId();

        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    ipAddressLoggingService.logSessionIPLookupFailure(userId, sessionId, "SESSION_NOT_FOUND",
                            responseTime, request);
                    return SessionNotFoundException.bySessionId(sessionId);
                });

        String ipAddress = session.getIpAddress();
        if (ipAddress == null || ipAddress.isBlank() || "unknown".equals(ipAddress)) {
            log.warn("Session {} has no valid IP address", sessionId);
            long responseTime = System.currentTimeMillis() - startTime;
            ipAddressLoggingService.logSessionIPLookupFailure(userId, sessionId, "NO_IP_ADDRESS",
                    responseTime, request);
            return null;
        }

        IPLookupResponse response;
        // If session already has geolocation data, return it
        if (session.getCountry() != null || session.getCity() != null) {
            response = IPLookupResponse.builder()
                    .ip(ipAddress)
                    .countryCode(session.getCountry())
                    .city(session.getCity())
                    .region(session.getRegion())
                    .latitude(session.getLatitude())
                    .longitude(session.getLongitude())
                    .timezone(session.getTimezone())
                    .isp(session.getIsp())
                    .build();
        } else {
            // Otherwise, perform fresh lookup
            IPGeolocationData geoData = ipGeolocationService.getGeolocation(ipAddress);
            if (geoData == null) {
                log.warn("No geolocation data found for session {} IP: {}", sessionId, ipAddress);
                long responseTime = System.currentTimeMillis() - startTime;
                ipAddressLoggingService.logSessionIPLookupFailure(userId, sessionId, "LOOKUP_FAILED",
                        responseTime, request);
                return null;
            }
            response = mapToIPLookupResponse(geoData);
        }

        long responseTime = System.currentTimeMillis() - startTime;
        ipAddressLoggingService.logSessionIPLookupSuccess(userId, sessionId, ipAddress, response,
                responseTime, request);
        return response;
    }

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        return null; // Should not happen due to @PreAuthorize, but handle gracefully
    }

    /**
     * Map IPGeolocationData to IPLookupResponse
     */
    private IPLookupResponse mapToIPLookupResponse(IPGeolocationData geoData) {
        return IPLookupResponse.builder()
                .ip(geoData.getIp())
                .country(geoData.getCountry())
                .countryCode(geoData.getCountryCode())
                .city(geoData.getCity())
                .region(geoData.getRegion())
                .latitude(geoData.getLatitude())
                .longitude(geoData.getLongitude())
                .timezone(geoData.getTimezone())
                .isp(geoData.getIsp())
                .postalCode(geoData.getPostalCode())
                .continent(geoData.getContinent())
                .currency(geoData.getCurrency())
                .callingCode(geoData.getCallingCode())
                .build();
    }
}
