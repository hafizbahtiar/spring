# IPLocate.io Integration Documentation

## Overview

[IPLocate.io](https://www.iplocate.io/) is a comprehensive IP geolocation and threat intelligence API service that provides detailed location data, threat detection, hosting provider information, and company data for any IP address. This service offers more advanced features compared to IPLocalize.com, including fraud detection, VPN/proxy detection, and hosting provider identification.

## Service Analysis

### Key Features

- **Comprehensive IP Intelligence**: One platform for geolocation, threat detection, hosting, and company data
- **Free Tier Available**: Free API key with usage limits
- **Advanced Threat Detection**: VPN, proxy, Tor detection, abuse reports
- **Hosting Provider Detection**: Identifies cloud services, datacenters, hosting providers
- **Company Information**: Organization name, domain, company type
- **Network Information**: ASN, AS organization, IP Whois data
- **High Performance**: <20ms average response time, 99.99% uptime
- **Daily Updates**: Databases updated daily for accuracy

### API Endpoint

```
GET https://iplocate.io/api/lookup/{ip_address}?apikey={api_key}
```

**Headers:**
```
Accept: application/json
```

**Query Parameters:**
- `apikey` (required): Your API key from IPLocate.io

### Response Format

The API returns comprehensive data including:

```json
{
  "ip": "17.253.0.0",
  "country": "United States",
  "country_code": "US",
  "continent": "North America",
  "city": "Cupertino",
  "region": "California",
  "postal_code": "95014",
  "latitude": 37.3230,
  "longitude": -122.0322,
  "timezone": "America/Los_Angeles",
  "currency": "USD",
  "calling_code": "+1",
  "isp": "Apple Inc.",
  "organization": "Apple Inc.",
  "asn": "AS714",
  "as_organization": "Apple Inc.",
  "hosting": {
    "is_hosting": true,
    "provider": "Apple Inc.",
    "service": "iCloud",
    "type": "cloud"
  },
  "threat": {
    "is_proxy": false,
    "is_vpn": false,
    "is_tor": false,
    "is_abuser": false
  },
  "company": {
    "name": "Apple Inc.",
    "domain": "apple.com",
    "type": "business"
  }
}
```

### Response Fields Mapping to Session Entity

| IPLocate Field | Session Entity Field | Type | Notes |
|---------------|---------------------|------|-------|
| `country_code` | `country` | String(10) | ISO country code (e.g., "US", "MY") |
| `region` | `region` | String(100) | State/Province name |
| `city` | `city` | String(100) | City name |
| `latitude` | `latitude` | Double | Decimal degrees |
| `longitude` | `longitude` | Double | Decimal degrees |
| `timezone` | `timezone` | String(50) | IANA timezone (e.g., "America/New_York") |
| `isp` or `organization` | `isp` | String(200) | Internet Service Provider or Organization name |

### Additional Data Available (Not in Session Entity)

IPLocate provides additional data that could be useful for:
- **Threat Detection**: `threat.is_proxy`, `threat.is_vpn`, `threat.is_tor`, `threat.is_abuser`
- **Hosting Detection**: `hosting.is_hosting`, `hosting.provider`, `hosting.service`
- **Company Information**: `company.name`, `company.domain`, `company.type`
- **Network Information**: `asn`, `as_organization`
- **Enhanced Location**: `postal_code`, `continent`, `currency`, `calling_code`

### Error Handling

- Returns standard HTTP status codes (200, 400, 401, 429, 500)
- Error responses include `error` field with message
- Rate limiting returns 429 status code
- Invalid API key returns 401 status code

## Pricing & Rate Limits

### Free Tier
- Requires API key (free registration)
- Limited requests per month (check current limits on website)
- All data types included

### Paid Tiers
- Higher rate limits
- More requests per month
- Priority support
- Custom packages available

**Note**: Check [IPLocate.io Pricing](https://www.iplocate.io/pricing) for current limits and pricing.

## Integration Strategy

### Adapter Pattern

IPLocate will be implemented as an adapter following the same pattern as payment providers:

1. **Adapter Interface**: `IPGeolocationAdapter`
2. **IPLocate Implementation**: `IPLocateAdapter`
3. **Provider Enum**: `IPGeolocationProvider.IPLOCATE`
4. **Main Service**: Uses adapter pattern to support multiple providers

### Configuration

```properties
# IPLocate.io Configuration
ip.geolocation.provider.iplocate.enabled=true
ip.geolocation.provider.iplocate.api-key=${IPLOCATE_API_KEY}
ip.geolocation.provider.iplocate.base-url=https://iplocate.io
ip.geolocation.provider.iplocate.timeout=5000
```

### API Key Setup

1. Sign up at [IPLocate.io](https://www.iplocate.io/)
2. Get your free API key from dashboard
3. Add to `application.properties` or environment variables
4. Configure in Spring Boot application

## Use Cases

### 1. Enhanced Security & Fraud Detection
- **VPN/Proxy Detection**: Identify users using VPNs or proxies
- **Tor Detection**: Detect Tor exit nodes
- **Abuse Detection**: Check if IP is on abuse blocklists
- **Hosting Detection**: Identify cloud/datacenter IPs (potential bots)

### 2. Advanced Session Tracking
- **Threat Context**: Store threat indicators with session
- **Hosting Context**: Identify if session is from hosting provider
- **Company Context**: Know which company/organization owns the IP

### 3. Compliance & Analytics
- **Regional Compliance**: Enhanced location data for tax/compliance
- **Network Analytics**: ASN and organization data for analytics
- **Threat Analytics**: Track VPN/proxy usage patterns

## Implementation Example

### IPLocate Adapter

```java
package com.hafizbahtiar.spring.common.service.ipgeolocation.adapter;

import com.hafizbahtiar.spring.common.dto.IPGeolocationData;
import com.hafizbahtiar.spring.common.service.ipgeolocation.IPGeolocationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class IPLocateAdapter implements IPGeolocationAdapter {
    
    private static final String API_BASE_URL = "https://iplocate.io/api/lookup";
    
    private final RestClient restClient;
    
    @Value("${ip.geolocation.provider.iplocate.api-key:}")
    private String apiKey;
    
    @Override
    public IPGeolocationProvider getProvider() {
        return IPGeolocationProvider.IPLOCATE;
    }
    
    @Override
    public IPGeolocationData getGeolocation(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank() || isPrivateIP(ipAddress)) {
            log.debug("Skipping IPLocate lookup for IP: {}", ipAddress);
            return null;
        }
        
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("IPLocate API key not configured");
            return null;
        }
        
        try {
            String url = String.format("%s/%s?apikey=%s", API_BASE_URL, ipAddress, apiKey);
            
            IPLocateResponse response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .body(IPLocateResponse.class);
            
            return mapToGeolocationData(response);
            
        } catch (Exception e) {
            log.warn("Failed to get geolocation from IPLocate for IP {}: {}", ipAddress, e.getMessage());
            return null;
        }
    }
    
    private IPGeolocationData mapToGeolocationData(IPLocateResponse response) {
        if (response == null) {
            return null;
        }
        
        IPGeolocationData data = new IPGeolocationData();
        data.setIp(response.getIp());
        data.setCountryCode(response.getCountryCode());
        data.setCountry(response.getCountry());
        data.setRegion(response.getRegion());
        data.setCity(response.getCity());
        data.setLatitude(response.getLatitude());
        data.setLongitude(response.getLongitude());
        data.setTimezone(response.getTimezone());
        
        // Use organization or isp, whichever is available
        String isp = response.getOrganization() != null 
                ? response.getOrganization() 
                : response.getIsp();
        data.setIsp(isp);
        
        return data;
    }
    
    private boolean isPrivateIP(String ip) {
        // Same private IP check as other adapters
        return ip == null || 
               ip.equals("unknown") ||
               ip.startsWith("127.") ||
               ip.startsWith("10.") ||
               ip.startsWith("192.168.") ||
               (ip.startsWith("172.") && isPrivate172Range(ip));
    }
    
    private boolean isPrivate172Range(String ip) {
        // 172.16.0.0 to 172.31.255.255
        String[] parts = ip.split("\\.");
        if (parts.length >= 2) {
            int secondOctet = Integer.parseInt(parts[1]);
            return secondOctet >= 16 && secondOctet <= 31;
        }
        return false;
    }
}

// Response DTO for IPLocate API
class IPLocateResponse {
    private String ip;
    private String country;
    private String countryCode;
    private String continent;
    private String city;
    private String region;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String currency;
    private String callingCode;
    private String isp;
    private String organization;
    private String asn;
    private String asOrganization;
    private HostingInfo hosting;
    private ThreatInfo threat;
    private CompanyInfo company;
    
    // Getters and setters
    // ... (omitted for brevity)
}

class HostingInfo {
    private Boolean isHosting;
    private String provider;
    private String service;
    private String type;
    // Getters and setters
}

class ThreatInfo {
    private Boolean isProxy;
    private Boolean isVpn;
    private Boolean isTor;
    private Boolean isAbuser;
    // Getters and setters
}

class CompanyInfo {
    private String name;
    private String domain;
    private String type;
    // Getters and setters
}
```

## Advantages Over IPLocalize

1. **Threat Detection**: Built-in VPN/proxy/Tor detection
2. **Hosting Detection**: Identifies cloud services and datacenters
3. **Company Data**: Organization and domain information
4. **Network Data**: ASN and network information
5. **More Accurate**: Daily updated databases, industry-leading accuracy
6. **Better Support**: Commercial support available

## Disadvantages

1. **Requires API Key**: Must register and get API key (free tier available)
2. **Rate Limits**: Free tier has stricter limits than IPLocalize
3. **More Complex**: More data fields to handle
4. **Potential Cost**: Paid tiers for higher usage

## When to Use IPLocate vs IPLocalize

### Use IPLocate When:
- You need threat detection (VPN/proxy/Tor)
- You need hosting provider detection
- You need company/organization data
- You need higher accuracy
- You have API key and can handle rate limits

### Use IPLocalize When:
- You need simple, no-configuration solution
- You don't need threat detection
- You want zero setup (no API key)
- You have lower usage requirements

## References

- [IPLocate.io Official Website](https://www.iplocate.io/)
- [API Documentation](https://www.iplocate.io/docs)
- [Pricing](https://www.iplocate.io/pricing)
- [Get Free API Key](https://www.iplocate.io/dashboard)

## Next Steps

1. ⏳ Register for IPLocate.io account
2. ⏳ Get API key from dashboard
3. ⏳ Configure API key in application properties
4. ⏳ Implement `IPLocateAdapter` following adapter pattern
5. ⏳ Add IPLocate to provider enum
6. ⏳ Configure as fallback or primary provider
7. ⏳ Test integration with real API calls
8. ⏳ Consider storing additional threat/hosting data in separate tables if needed

