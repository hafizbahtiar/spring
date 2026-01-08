package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when a certification is not found.
 */
public class CertificationNotFoundException extends RuntimeException {

    public CertificationNotFoundException(String message) {
        super(message);
    }

    public static CertificationNotFoundException byId(Long certificationId) {
        return new CertificationNotFoundException("Certification not found with ID: " + certificationId);
    }

    public static CertificationNotFoundException byIdAndUser(Long certificationId, Long userId) {
        return new CertificationNotFoundException(
                "Certification not found with ID: " + certificationId + " for user ID: " + userId);
    }
}
