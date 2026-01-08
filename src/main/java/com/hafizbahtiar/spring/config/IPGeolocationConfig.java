package com.hafizbahtiar.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Configuration for IP geolocation services.
 * Provides RestClient bean for making HTTP requests to IP geolocation
 * providers.
 */
@Configuration
public class IPGeolocationConfig {

    /**
     * RestClient bean for IP geolocation API calls.
     * Configured with default headers for JSON responses.
     */
    @Bean
    public RestClient ipGeolocationRestClient() {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
