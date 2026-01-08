package com.hafizbahtiar.spring.features.ipaddress.exception;

/**
 * Base exception for IP address related operations.
 */
public class IPAddressException extends RuntimeException {

    public IPAddressException(String message) {
        super(message);
    }

    public IPAddressException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create exception for invalid IP address
     */
    public static IPAddressException invalidIP(String ipAddress) {
        return new IPAddressException("Invalid IP address: " + ipAddress);
    }

    /**
     * Create exception for IP lookup failure
     */
    public static IPAddressException lookupFailed(String ipAddress) {
        return new IPAddressException("Failed to lookup geolocation for IP address: " + ipAddress);
    }

    /**
     * Create exception for session not found
     */
    public static IPAddressException sessionNotFound(String sessionId) {
        return new IPAddressException("Session not found: " + sessionId);
    }
}
