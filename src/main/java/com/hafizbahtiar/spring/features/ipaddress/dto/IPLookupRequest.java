package com.hafizbahtiar.spring.features.ipaddress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for IP address lookup.
 */
@Data
public class IPLookupRequest {

    /**
     * IP address to lookup (IPv4 or IPv6)
     */
    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$", message = "Invalid IP address format")
    private String ipAddress;
}
