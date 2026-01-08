package com.hafizbahtiar.spring.features.ipaddress.service;

import com.hafizbahtiar.spring.features.ipaddress.dto.IPLookupResponse;
import com.hafizbahtiar.spring.features.ipaddress.model.IPLookupLog;
import com.hafizbahtiar.spring.features.ipaddress.repository.mongodb.IPLookupLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for logging IP address lookup events to MongoDB.
 * Provides methods to log IP geolocation lookup events for audit and security
 * purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IPAddressLoggingService {

    private final IPLookupLogRepository ipLookupLogRepository;

    /**
     * Log a successful IP lookup event
     */
    @Async
    public void logIPLookupSuccess(Long userId, String lookedUpIp, IPLookupResponse geolocationData,
            Long responseTimeMs, HttpServletRequest request) {
        try {
            Map<String, Object> geoDataMap = new HashMap<>();
            if (geolocationData != null) {
                geoDataMap.put("country", geolocationData.getCountry());
                geoDataMap.put("countryCode", geolocationData.getCountryCode());
                geoDataMap.put("city", geolocationData.getCity());
                geoDataMap.put("region", geolocationData.getRegion());
                geoDataMap.put("latitude", geolocationData.getLatitude());
                geoDataMap.put("longitude", geolocationData.getLongitude());
                geoDataMap.put("timezone", geolocationData.getTimezone());
                geoDataMap.put("isp", geolocationData.getIsp());
            }

            IPLookupLog lookupLog = IPLookupLog.builder()
                    .eventType("IP_LOOKUP")
                    .userId(userId)
                    .lookedUpIp(lookedUpIp)
                    .timestamp(LocalDateTime.now())
                    .clientIpAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .geolocationData(geoDataMap)
                    .responseTimeMs(responseTimeMs)
                    .build();

            ipLookupLogRepository.save(lookupLog);
            log.debug("Logged successful IP lookup for IP: {} by user: {}", lookedUpIp, userId);
        } catch (Exception e) {
            log.error("Failed to log IP lookup success event", e);
            // Don't throw exception - logging failure shouldn't break lookup flow
        }
    }

    /**
     * Log a failed IP lookup event
     */
    @Async
    public void logIPLookupFailure(Long userId, String lookedUpIp, String failureReason,
            Long responseTimeMs, HttpServletRequest request) {
        try {
            IPLookupLog lookupLog = IPLookupLog.builder()
                    .eventType("IP_LOOKUP")
                    .userId(userId)
                    .lookedUpIp(lookedUpIp)
                    .timestamp(LocalDateTime.now())
                    .clientIpAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(false)
                    .failureReason(failureReason)
                    .responseTimeMs(responseTimeMs)
                    .build();

            ipLookupLogRepository.save(lookupLog);
            log.debug("Logged failed IP lookup for IP: {} by user: {}, reason: {}", lookedUpIp, userId, failureReason);
        } catch (Exception e) {
            log.error("Failed to log IP lookup failure event", e);
            // Don't throw exception - logging failure shouldn't break lookup flow
        }
    }

    /**
     * Log a successful session IP lookup event
     */
    @Async
    public void logSessionIPLookupSuccess(Long userId, String sessionId, String lookedUpIp,
            IPLookupResponse geolocationData, Long responseTimeMs, HttpServletRequest request) {
        try {
            Map<String, Object> geoDataMap = new HashMap<>();
            if (geolocationData != null) {
                geoDataMap.put("country", geolocationData.getCountry());
                geoDataMap.put("countryCode", geolocationData.getCountryCode());
                geoDataMap.put("city", geolocationData.getCity());
                geoDataMap.put("region", geolocationData.getRegion());
                geoDataMap.put("latitude", geolocationData.getLatitude());
                geoDataMap.put("longitude", geolocationData.getLongitude());
                geoDataMap.put("timezone", geolocationData.getTimezone());
                geoDataMap.put("isp", geolocationData.getIsp());
            }

            IPLookupLog lookupLog = IPLookupLog.builder()
                    .eventType("SESSION_IP_LOOKUP")
                    .userId(userId)
                    .sessionId(sessionId)
                    .lookedUpIp(lookedUpIp)
                    .timestamp(LocalDateTime.now())
                    .clientIpAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(true)
                    .geolocationData(geoDataMap)
                    .responseTimeMs(responseTimeMs)
                    .build();

            ipLookupLogRepository.save(lookupLog);
            log.debug("Logged successful session IP lookup for session: {} by user: {}", sessionId, userId);
        } catch (Exception e) {
            log.error("Failed to log session IP lookup success event", e);
            // Don't throw exception - logging failure shouldn't break lookup flow
        }
    }

    /**
     * Log a failed session IP lookup event
     */
    @Async
    public void logSessionIPLookupFailure(Long userId, String sessionId, String failureReason,
            Long responseTimeMs, HttpServletRequest request) {
        try {
            IPLookupLog lookupLog = IPLookupLog.builder()
                    .eventType("SESSION_IP_LOOKUP")
                    .userId(userId)
                    .sessionId(sessionId)
                    .timestamp(LocalDateTime.now())
                    .clientIpAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(false)
                    .failureReason(failureReason)
                    .responseTimeMs(responseTimeMs)
                    .build();

            ipLookupLogRepository.save(lookupLog);
            log.debug("Logged failed session IP lookup for session: {} by user: {}, reason: {}",
                    sessionId, userId, failureReason);
        } catch (Exception e) {
            log.error("Failed to log session IP lookup failure event", e);
            // Don't throw exception - logging failure shouldn't break lookup flow
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle comma-separated IPs (from proxies)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Get user agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : "unknown";
    }

    /**
     * Get request ID from request (if available)
     */
    private String getRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // Try to get request ID from header or attribute
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = (String) request.getAttribute("requestId");
        }
        return requestId;
    }
}
