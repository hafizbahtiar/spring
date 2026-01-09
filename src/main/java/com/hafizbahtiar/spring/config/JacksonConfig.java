package com.hafizbahtiar.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration for JSON serialization/deserialization.
 * Provides ObjectMapper bean for use throughout the application.
 * 
 * Configures ObjectMapper with JavaTimeModule for Java 8 time types support
 * (LocalDateTime, etc.) needed for Socket.IO and Redis caching.
 */
@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper bean for JSON processing.
     * Registers JavaTimeModule for Java 8 time types support.
     * Disables WRITE_DATES_AS_TIMESTAMPS to serialize dates as ISO-8601 strings.
     *
     * @return Configured ObjectMapper instance with JavaTimeModule
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
