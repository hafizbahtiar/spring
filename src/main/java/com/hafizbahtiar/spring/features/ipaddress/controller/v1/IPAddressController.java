package com.hafizbahtiar.spring.features.ipaddress.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.ipaddress.dto.IPLookupRequest;
import com.hafizbahtiar.spring.features.ipaddress.dto.IPLookupResponse;
import com.hafizbahtiar.spring.features.ipaddress.service.IPAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for IP address lookup endpoints.
 * Provides geolocation lookup functionality for owner/admin users.
 */
@RestController
@RequestMapping("/api/v1/ip-address")
@RequiredArgsConstructor
@Slf4j
public class IPAddressController {

    private final IPAddressService ipAddressService;

    /**
     * Lookup geolocation for an IP address.
     * POST /api/v1/ip-address/lookup
     * Requires: OWNER or ADMIN role
     * 
     * @param request     IP lookup request containing IP address
     * @param httpRequest HTTP request for logging context
     * @return IPLookupResponse with geolocation data
     */
    @PostMapping("/lookup")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<IPLookupResponse>> lookupIP(
            @Valid @RequestBody IPLookupRequest request,
            HttpServletRequest httpRequest) {
        log.info("IP lookup request received for IP: {}", request.getIpAddress());

        IPLookupResponse response = ipAddressService.lookupIP(request.getIpAddress(), httpRequest);

        if (response == null) {
            log.warn("No geolocation data found for IP: {}", request.getIpAddress());
            return ResponseUtils.ok(null, "No geolocation data available for this IP address");
        }

        return ResponseUtils.ok(response);
    }

    /**
     * Get geolocation for a specific IP address.
     * GET /api/v1/ip-address/lookup/{ipAddress}
     * Requires: OWNER or ADMIN role
     * 
     * @param ipAddress   IP address to lookup
     * @param httpRequest HTTP request for logging context
     * @return IPLookupResponse with geolocation data
     */
    @GetMapping("/lookup/{ipAddress}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<IPLookupResponse>> lookupIPByPath(
            @PathVariable String ipAddress,
            HttpServletRequest httpRequest) {
        log.info("IP lookup request received for IP: {}", ipAddress);

        IPLookupResponse response = ipAddressService.lookupIP(ipAddress, httpRequest);

        if (response == null) {
            log.warn("No geolocation data found for IP: {}", ipAddress);
            return ResponseUtils.ok(null, "No geolocation data available for this IP address");
        }

        return ResponseUtils.ok(response);
    }

    /**
     * Get geolocation for a session's IP address.
     * GET /api/v1/ip-address/sessions/{sessionId}
     * Requires: OWNER or ADMIN role
     * 
     * @param sessionId   Session ID
     * @param httpRequest HTTP request for logging context
     * @return IPLookupResponse with geolocation data
     */
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<IPLookupResponse>> getSessionIPGeolocation(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        log.info("Session IP geolocation request received for session: {}", sessionId);

        IPLookupResponse response = ipAddressService.getSessionIPGeolocation(sessionId, httpRequest);

        if (response == null) {
            log.warn("No geolocation data found for session: {}", sessionId);
            return ResponseUtils.ok(null, "No geolocation data available for this session");
        }

        return ResponseUtils.ok(response);
    }
}
