# IP Geolocation Service Architecture

## Overview

This document describes the architecture for IP geolocation services in the Spring Boot application. The system is designed to support multiple IP geolocation providers using the **Adapter Pattern** and **Strategy Pattern**, allowing easy switching between providers and fallback mechanisms.

## Architecture Pattern

Following the same pattern used in the payment and subscription modules, the IP geolocation system uses:

1. **Adapter Pattern**: Each provider implements a common adapter interface
2. **Strategy Pattern**: Main service selects and uses appropriate adapter
3. **Fallback Strategy**: Automatic fallback to secondary providers on failure
4. **Provider Enum**: Type-safe provider identification

## Directory Structure

```
src/main/java/com/hafizbahtiar/spring/
├── common/
│   ├── service/
│   │   └── ipgeolocation/
│   │       ├── IPGeolocationService.java          # Main service interface
│   │       ├── IPGeolocationServiceImpl.java     # Main service implementation
│   │       ├── adapter/
│   │       │   ├── IPGeolocationAdapter.java     # Adapter interface
│   │       │   ├── IPLocalizeAdapter.java        # IPLocalize.com adapter
│   │       │   └── IPLocateAdapter.java           # IPLocate.io adapter
│   │       ├── dto/
│   │       │   └── IPGeolocationData.java        # Common DTO for all providers
│   │       ├── enum/
│   │       │   └── IPGeolocationProvider.java     # Provider enum
│   │       └── exception/
│   │           └── IPGeolocationException.java   # Custom exceptions
│   └── dto/
│       └── IPGeolocationData.java                # (Alternative location)
```

## Component Design

### 1. IPGeolocationProvider Enum

Defines all supported providers:

```java
package com.hafizbahtiar.spring.common.service.ipgeolocation.enum;

public enum IPGeolocationProvider {
    IPLOCALIZE("iplocalize", "IPLocalize.com"),
    IPLOCATE("iplocate", "IPLocate.io");
    
    private final String code;
    private final String displayName;
    
    // Constructor, getters
}
```

### 2. IPGeolocationAdapter Interface

Common interface that all provider adapters must implement:

```java
package com.hafizbahtiar.spring.common.service.ipgeolocation.adapter;

import com.hafizbahtiar.spring.common.dto.IPGeolocationData;
import com.hafizbahtiar.spring.common.service.ipgeolocation.enum.IPGeolocationProvider;

public interface IPGeolocationAdapter {
    /**
     * Get the provider this adapter handles
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
     */
    boolean isEnabled();
}
```

### 3. IPGeolocationData DTO

Common data transfer object that maps to Session entity:

```java
package com.hafizbahtiar.spring.common.dto;

import lombok.Data;

@Data
public class IPGeolocationData {
    private String ip;
    private String country;           // Full country name
    private String countryCode;       // ISO country code (e.g., "US", "MY")
    private String city;              // City name
    private String region;            // State/Province name
    private Double latitude;          // Decimal degrees
    private Double longitude;         // Decimal degrees
    private String timezone;          // IANA timezone (e.g., "America/New_York")
    private String isp;               // Internet Service Provider name
    
    // Optional: Additional fields for future use
    private String postalCode;
    private String continent;
    private String currency;
    private String callingCode;
}
```

### 4. Adapter Implementations

Each provider has its own adapter that:
- Implements `IPGeolocationAdapter`
- Handles provider-specific API calls
- Maps provider response to `IPGeolocationData`
- Handles provider-specific errors

**IPLocalizeAdapter**:
- No API key required
- Simple REST API
- Maps `country_code` → `countryCode`

**IPLocateAdapter**:
- Requires API key
- More comprehensive data
- Maps `organization` or `isp` → `isp`
- Can extract threat/hosting data (future enhancement)

### 5. IPGeolocationService (Main Service)

Orchestrates adapter selection and fallback:

```java
package com.hafizbahtiar.spring.common.service.ipgeolocation;

import com.hafizbahtiar.spring.common.dto.IPGeolocationData;
import java.util.concurrent.CompletableFuture;

public interface IPGeolocationService {
    /**
     * Get geolocation data using configured primary provider
     */
    IPGeolocationData getGeolocation(String ipAddress);
    
    /**
     * Get geolocation data asynchronously
     */
    CompletableFuture<IPGeolocationData> getGeolocationAsync(String ipAddress);
    
    /**
     * Get geolocation data from specific provider
     */
    IPGeolocationData getGeolocation(String ipAddress, IPGeolocationProvider provider);
}
```

### 6. IPGeolocationServiceImpl

Main service implementation with fallback logic:

```java
package com.hafizbahtiar.spring.common.service.ipgeolocation;

import com.hafizbahtiar.spring.common.dto.IPGeolocationData;
import com.hafizbahtiar.spring.common.service.ipgeolocation.adapter.IPGeolocationAdapter;
import com.hafizbahtiar.spring.common.service.ipgeolocation.enum.IPGeolocationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class IPGeolocationServiceImpl implements IPGeolocationService {
    
    private final List<IPGeolocationAdapter> adapters;
    private final Executor taskExecutor;
    
    @Value("${ip.geolocation.provider.primary:IPLOCALIZE}")
    private IPGeolocationProvider primaryProvider;
    
    @Value("${ip.geolocation.provider.fallback:IPLOCATE}")
    private IPGeolocationProvider fallbackProvider;
    
    @Value("${ip.geolocation.enable-fallback:true}")
    private boolean enableFallback;
    
    @Override
    public IPGeolocationData getGeolocation(String ipAddress) {
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
        if (enableFallback && fallbackProvider != null && !fallbackProvider.equals(primaryProvider)) {
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
        
        log.warn("All geolocation providers failed for IP: {}", ipAddress);
        return null;
    }
    
    @Override
    public CompletableFuture<IPGeolocationData> getGeolocationAsync(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> getGeolocation(ipAddress), taskExecutor);
    }
    
    @Override
    public IPGeolocationData getGeolocation(String ipAddress, IPGeolocationProvider provider) {
        IPGeolocationAdapter adapter = getAdapter(provider);
        if (adapter == null || !adapter.isEnabled()) {
            log.warn("Provider {} is not available or not enabled", provider);
            return null;
        }
        
        return adapter.getGeolocation(ipAddress);
    }
    
    private IPGeolocationAdapter getAdapter(IPGeolocationProvider provider) {
        return adapters.stream()
                .filter(adapter -> adapter.getProvider() == provider)
                .findFirst()
                .orElse(null);
    }
    
    private boolean isPrivateIP(String ip) {
        // Centralized private IP check
        return ip == null || 
               ip.equals("unknown") ||
               ip.startsWith("127.") ||
               ip.startsWith("10.") ||
               ip.startsWith("192.168.") ||
               (ip.startsWith("172.") && isPrivate172Range(ip));
    }
    
    private boolean isPrivate172Range(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length >= 2) {
            try {
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
}
```

## Integration with Session Service

### SessionServiceImpl Integration

```java
// In SessionServiceImpl.createSession()

// Extract request information
String userAgent = getUserAgent(request);
String ipAddress = getClientIpAddress(request);

// Create session
Session session = new Session(user, userAgent, ipAddress);

// Parse user agent to extract device info
parseUserAgent(userAgent, session);

// Get IP geolocation data (async to not block session creation)
ipGeolocationService.getGeolocationAsync(ipAddress)
    .thenAccept(geoData -> {
        if (geoData != null) {
            session.setCountry(geoData.getCountryCode());
            session.setRegion(geoData.getRegion());
            session.setCity(geoData.getCity());
            session.setLatitude(geoData.getLatitude());
            session.setLongitude(geoData.getLongitude());
            session.setTimezone(geoData.getTimezone());
            session.setIsp(geoData.getIsp());
            sessionRepository.save(session);
            log.debug("Updated session {} with geolocation data from {}", 
                    session.getSessionId(), 
                    geoData.getProvider()); // If we add provider tracking
        }
    })
    .exceptionally(ex -> {
        log.warn("Failed to enrich session with geolocation: {}", ex.getMessage());
        return null;
    });

Session savedSession = sessionRepository.save(session);
```

## Configuration

### application.properties

```properties
# IP Geolocation Configuration
ip.geolocation.enabled=true
ip.geolocation.enable-fallback=true
ip.geolocation.provider.primary=IPLOCALIZE
ip.geolocation.provider.fallback=IPLOCATE

# IPLocalize.com Configuration
ip.geolocation.provider.iplocalize.enabled=true
ip.geolocation.provider.iplocalize.base-url=https://iplocalize.com
ip.geolocation.provider.iplocalize.timeout=5000

# IPLocate.io Configuration
ip.geolocation.provider.iplocate.enabled=true
ip.geolocation.provider.iplocate.api-key=${IPLOCATE_API_KEY:}
ip.geolocation.provider.iplocate.base-url=https://iplocate.io
ip.geolocation.provider.iplocate.timeout=5000
```

## Adapter Responsibilities

### What Each Adapter Must Do

1. **Implement IPGeolocationAdapter Interface**
   - Return correct provider enum
   - Implement `getGeolocation()` method
   - Implement `isEnabled()` check

2. **Handle Provider-Specific API Calls**
   - Make HTTP requests to provider API
   - Handle authentication (API keys, headers)
   - Parse provider-specific response format

3. **Map to Common DTO**
   - Convert provider response to `IPGeolocationData`
   - Handle field name differences (e.g., `country_code` vs `countryCode`)
   - Handle missing/null fields gracefully

4. **Error Handling**
   - Catch and log provider-specific errors
   - Return null on failure (don't throw exceptions)
   - Handle rate limits appropriately

5. **Private IP Detection**
   - Skip lookups for private/localhost IPs
   - Return null immediately for private IPs

## Adding New Providers

### Steps to Add a New Provider

1. **Add Provider to Enum**
   ```java
   NEW_PROVIDER("newprovider", "New Provider Name")
   ```

2. **Create Adapter Implementation**
   ```java
   @Component
   public class NewProviderAdapter implements IPGeolocationAdapter {
       // Implement interface methods
   }
   ```

3. **Add Configuration Properties**
   ```properties
   ip.geolocation.provider.newprovider.enabled=true
   ip.geolocation.provider.newprovider.api-key=${NEW_PROVIDER_API_KEY:}
   ```

4. **Spring Auto-Discovery**
   - Adapter will be automatically injected into service
   - No manual registration needed (thanks to `@Component`)

## Benefits of This Architecture

### 1. **Extensibility**
- Easy to add new providers
- No changes to main service code
- Follows Open/Closed Principle

### 2. **Flexibility**
- Switch providers via configuration
- Use different providers for different use cases
- Enable/disable providers independently

### 3. **Resilience**
- Automatic fallback on failure
- Graceful degradation
- No single point of failure

### 4. **Testability**
- Easy to mock adapters
- Test each adapter independently
- Test fallback logic separately

### 5. **Consistency**
- Follows same pattern as payment/subscription modules
- Familiar structure for developers
- Consistent error handling

## Testing Strategy

### Unit Tests

1. **Adapter Tests**
   - Test each adapter independently
   - Mock HTTP responses
   - Test field mapping
   - Test error handling

2. **Service Tests**
   - Test provider selection
   - Test fallback logic
   - Test private IP handling
   - Mock adapters

### Integration Tests

1. **Real API Tests**
   - Test with actual provider APIs
   - Test rate limiting
   - Test error scenarios
   - Use test IP addresses

2. **End-to-End Tests**
   - Test session enrichment flow
   - Test async processing
   - Test database persistence

## Future Enhancements

### 1. Caching Layer
- Cache geolocation results by IP
- Reduce API calls
- Configurable TTL

### 2. Circuit Breaker
- Prevent cascading failures
- Automatic recovery
- Health monitoring

### 3. Metrics & Monitoring
- Track provider usage
- Monitor success/failure rates
- Track response times
- Alert on failures

### 4. Additional Data Storage
- Store threat detection data
- Store hosting provider info
- Store company information
- Separate tables for extended data

### 5. Batch Processing
- Lookup multiple IPs at once
- Optimize for bulk operations
- Reduce API calls

## References

- [IPLocalize.com Documentation](../ip-address/iplocalize.md)
- [IPLocate.io Documentation](../ip-address/iplocate.md)
- [Payment Provider Pattern](../../src/main/java/com/hafizbahtiar/spring/features/payment/provider/PaymentProviderService.java)
- [Subscription Provider Pattern](../../src/main/java/com/hafizbahtiar/spring/features/subscription/provider/SubscriptionProviderService.java)

