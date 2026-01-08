package com.hafizbahtiar.spring.features.ipaddress.provider.iplocalize;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hafizbahtiar.spring.features.ipaddress.dto.IPGeolocationData;
import com.hafizbahtiar.spring.features.ipaddress.entity.IPGeolocationProvider;
import com.hafizbahtiar.spring.features.ipaddress.provider.IPGeolocationAdapter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adapter for IPLocalize.com IP geolocation service.
 * 
 * Features:
 * - Free service, no API key required
 * - 60 requests/minute rate limit per IP
 * - Simple REST API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IPLocalizeAdapter implements IPGeolocationAdapter {

    private final RestClient restClient;

    @Value("${ip.geolocation.provider.iplocalize.enabled:true}")
    private boolean enabled;

    @Value("${ip.geolocation.provider.iplocalize.base-url:https://iplocalize.com}")
    private String baseUrl;

    @Override
    public IPGeolocationProvider getProvider() {
        return IPGeolocationProvider.IPLOCALIZE;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public IPGeolocationData getGeolocation(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank() || isPrivateIP(ipAddress)) {
            log.debug("Skipping IPLocalize lookup for IP: {}", ipAddress);
            return null;
        }

        if (!enabled) {
            log.debug("IPLocalize adapter is disabled");
            return null;
        }

        try {
            String url = String.format("%s/api/v1/lookup/%s", baseUrl, ipAddress);

            IPLocalizeResponse response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .body(IPLocalizeResponse.class);

            if (response == null) {
                log.warn("IPLocalize returned null response for IP: {}", ipAddress);
                return null;
            }

            return mapToGeolocationData(response);

        } catch (Exception e) {
            log.warn("Failed to get geolocation from IPLocalize for IP {}: {}", ipAddress, e.getMessage());
            return null;
        }
    }

    /**
     * Map IPLocalize API response to common IPGeolocationData DTO
     */
    private IPGeolocationData mapToGeolocationData(IPLocalizeResponse response) {
        IPGeolocationData data = new IPGeolocationData();
        data.setIp(response.getIp());
        data.setCountry(response.getCountry());
        data.setCountryCode(response.getCountryCode());
        data.setCity(response.getCity());
        data.setRegion(response.getRegion());
        data.setLatitude(response.getLatitude());
        data.setLongitude(response.getLongitude());
        data.setTimezone(response.getTimezone());
        data.setIsp(response.getIsp());
        return data;
    }

    /**
     * Check if IP address is private/localhost (should skip geolocation lookup)
     */
    private boolean isPrivateIP(String ip) {
        if (ip == null || ip.isBlank() || ip.equals("unknown")) {
            return true;
        }

        // Localhost
        if (ip.startsWith("127.")) {
            return true;
        }

        // Private IP ranges
        if (ip.startsWith("10.")) {
            return true;
        }

        if (ip.startsWith("192.168.")) {
            return true;
        }

        // 172.16.0.0 to 172.31.255.255
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int secondOctet = Integer.parseInt(parts[1]);
                    return secondOctet >= 16 && secondOctet <= 31;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Response DTO for IPLocalize.com API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IPLocalizeResponse {
        private String ip;
        private String country;

        @JsonProperty("country_code")
        private String countryCode;

        private String city;
        private String region;
        private Double latitude;
        private Double longitude;
        private String timezone;
        private String isp;
    }
}
