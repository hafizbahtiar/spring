package com.hafizbahtiar.spring.common.exception;

/**
 * Exception thrown when payment provider operations fail.
 * Used for provider-specific errors (Stripe, PayPal, etc.).
 */
public class ProviderException extends RuntimeException {

    private final String provider;
    private final String providerErrorCode;

    public ProviderException(String message) {
        super(message);
        this.provider = null;
        this.providerErrorCode = null;
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
        this.provider = null;
        this.providerErrorCode = null;
    }

    public ProviderException(String provider, String message) {
        super(message);
        this.provider = provider;
        this.providerErrorCode = null;
    }

    public ProviderException(String provider, String message, String providerErrorCode) {
        super(message);
        this.provider = provider;
        this.providerErrorCode = providerErrorCode;
    }

    public ProviderException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.providerErrorCode = null;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderErrorCode() {
        return providerErrorCode;
    }

    public static ProviderException notSupported(String provider) {
        return new ProviderException(provider, "Payment provider not supported: " + provider);
    }

    public static ProviderException apiError(String provider, String reason) {
        return new ProviderException(provider, "Provider API error: " + reason);
    }

    public static ProviderException apiError(String provider, String reason, String errorCode) {
        return new ProviderException(provider, "Provider API error: " + reason, errorCode);
    }

    public static ProviderException configurationError(String provider, String reason) {
        return new ProviderException(provider, "Provider configuration error: " + reason);
    }

    public static ProviderException webhookError(String provider, String reason) {
        return new ProviderException(provider, "Webhook processing error: " + reason);
    }
}
