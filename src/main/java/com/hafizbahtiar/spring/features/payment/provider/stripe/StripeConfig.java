package com.hafizbahtiar.spring.features.payment.provider.stripe;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Stripe integration.
 * Loads Stripe API keys from application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
public class StripeConfig {

    /**
     * Stripe Secret Key (sk_test_xxx or sk_live_xxx)
     */
    private String secretKey;

    /**
     * Stripe Publishable Key (pk_test_xxx or pk_live_xxx)
     */
    private String publishableKey;

    /**
     * Stripe Webhook Secret (whsec_xxx) for webhook signature verification
     */
    private String webhookSecret;
}
