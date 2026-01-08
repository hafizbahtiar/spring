package com.hafizbahtiar.spring.features.ipaddress.service;

import com.hafizbahtiar.spring.features.ipaddress.dto.IPGeolocationData;
import com.hafizbahtiar.spring.features.ipaddress.entity.IPGeolocationProvider;
import com.hafizbahtiar.spring.features.ipaddress.provider.IPGeolocationAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Implementation of IPGeolocationService.
 * Handles IP geolocation lookups using adapter pattern with fallback support.
 */
@Slf4j
@Service
public class IPGeolocationServiceImpl implements IPGeolocationService {

    private final List<IPGeolocationAdapter> adapters;
    private final Executor taskExecutor;

    @Value("${ip.geolocation.provider.primary:IPLOCALIZE}")
    private IPGeolocationProvider primaryProvider;

    @Value("${ip.geolocation.provider.fallback:}")
    private String fallbackProviderStr;

    @Value("${ip.geolocation.enable-fallback:false}")
    private boolean enableFallback;

    @Value("${ip.geolocation.enabled:true}")
    private boolean enabled;

    /**
     * Constructor with dependency injection.
     * Uses @Qualifier to specify which Executor bean to inject.
     */
    public IPGeolocationServiceImpl(
            List<IPGeolocationAdapter> adapters,
            @Qualifier("applicationTaskExecutor") Executor taskExecutor) {
        this.adapters = adapters;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public IPGeolocationData getGeolocation(String ipAddress) {
        if (!enabled) {
            log.debug("IP geolocation is disabled");
            return null;
        }

        if (ipAddress == null || ipAddress.isBlank() || isPrivateIP(ipAddress)) {
            log.debug("Skipping geolocation lookup for IP: {}", ipAddress);
            return null;
        }

        // Try primary provider
        IPGeolocationAdapter primaryAdapter = getAdapter(primaryProvider);
        if (primaryAdapter != null && primaryAdapter.isEnabled()) {
            try {
                IPGeolocationData data = primaryAdapter.getGeolocation(ipAddress);
                if (data != null) {
                    log.debug("Successfully retrieved geolocation from {} for IP: {}",
                            primaryProvider, ipAddress);
                    return data;
                }
            } catch (Exception e) {
                log.warn("Primary provider {} failed for IP {}: {}",
                        primaryProvider, ipAddress, e.getMessage());
            }
        }

        // Fallback to secondary provider if enabled
        if (enableFallback && fallbackProviderStr != null && !fallbackProviderStr.isBlank()) {
            try {
                IPGeolocationProvider fallbackProvider = IPGeolocationProvider.fromString(fallbackProviderStr);
                if (fallbackProvider != null && !fallbackProvider.equals(primaryProvider)) {
                    IPGeolocationAdapter fallbackAdapter = getAdapter(fallbackProvider);
                    if (fallbackAdapter != null && fallbackAdapter.isEnabled()) {
                        try {
                            IPGeolocationData data = fallbackAdapter.getGeolocation(ipAddress);
                            if (data != null) {
                                log.debug("Successfully retrieved geolocation from fallback {} for IP: {}",
                                        fallbackProvider, ipAddress);
                                return data;
                            }
                        } catch (Exception e) {
                            log.warn("Fallback provider {} failed for IP {}: {}",
                                    fallbackProvider, ipAddress, e.getMessage());
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid fallback provider configured: {}", fallbackProviderStr);
            }
        }

        log.warn("All geolocation providers failed for IP: {}", ipAddress);
        return null;
    }

    @Override
    public CompletableFuture<IPGeolocationData> getGeolocationAsync(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> getGeolocation(ipAddress), taskExecutor);
    }

    @Override
    public IPGeolocationData getGeolocation(String ipAddress, IPGeolocationProvider provider) {
        if (!enabled) {
            log.debug("IP geolocation is disabled");
            return null;
        }

        IPGeolocationAdapter adapter = getAdapter(provider);
        if (adapter == null || !adapter.isEnabled()) {
            log.warn("Provider {} is not available or not enabled", provider);
            return null;
        }

        return adapter.getGeolocation(ipAddress);
    }

    /**
     * Get adapter for a specific provider
     */
    private IPGeolocationAdapter getAdapter(IPGeolocationProvider provider) {
        return adapters.stream()
                .filter(adapter -> adapter.getProvider() == provider)
                .findFirst()
                .orElse(null);
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
}
