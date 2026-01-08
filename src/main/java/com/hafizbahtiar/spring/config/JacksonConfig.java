package com.hafizbahtiar.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration for JSON serialization/deserialization.
 * Provides ObjectMapper bean for use throughout the application.
 * 
 * Spring Boot auto-configures ObjectMapper, but we provide this as a fallback
 * to ensure it's always available as a bean.
 */
@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper bean for JSON processing.
     * Provides ObjectMapper for use throughout the application.
     *
     * @return Configured ObjectMapper instance
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
