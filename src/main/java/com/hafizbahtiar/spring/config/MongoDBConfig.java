package com.hafizbahtiar.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * MongoDB configuration.
 * Handles MongoDB connection URI construction with optional authentication.
 * 
 * If MONGODB_URI is provided, it will be used as-is.
 * If MONGODB_URI is not provided but MONGODB_USERNAME and MONGODB_PASSWORD are
 * set,
 * the URI will be constructed with credentials.
 */
@Configuration
@Slf4j
public class MongoDBConfig {

    @Value("${spring.data.mongodb.host:127.0.0.1}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port:27017}")
    private int mongoPort;

    @Value("${spring.data.mongodb.database:console}")
    private String mongoDatabase;

    @Value("${spring.data.mongodb.username:}")
    private String mongoUsername;

    @Value("${spring.data.mongodb.password:}")
    private String mongoPassword;

    @Value("${spring.data.mongodb.authentication-database:}")
    private String mongoAuthDatabase;

    /**
     * Constructs MongoDB URI with credentials if username/password are provided.
     * This allows using either MONGODB_URI directly or separate username/password
     * properties.
     */
    @Bean
    @Primary
    public MongoDatabaseFactory mongoDatabaseFactory() {
        String uri = buildMongoUri();
        log.info("MongoDB connection URI: {}", maskPassword(uri));
        return new SimpleMongoClientDatabaseFactory(uri);
    }

    /**
     * Builds MongoDB URI from configuration.
     * Priority:
     * 1. Use MONGODB_URI if provided (and not empty)
     * 2. Construct URI with credentials if username/password are provided
     * 3. Use URI without credentials as fallback
     */
    private String buildMongoUri() {

        // If username and password are provided, construct URI with credentials
        if (mongoUsername != null && !mongoUsername.isEmpty()
                && mongoPassword != null && !mongoPassword.isEmpty()) {

            String encodedUsername = URLEncoder.encode(mongoUsername, StandardCharsets.UTF_8);
            String encodedPassword = URLEncoder.encode(mongoPassword, StandardCharsets.UTF_8);

            StringBuilder uriBuilder = new StringBuilder("mongodb://");
            uriBuilder.append(encodedUsername).append(":").append(encodedPassword);
            uriBuilder.append("@").append(mongoHost).append(":").append(mongoPort);
            uriBuilder.append("/").append(mongoDatabase);

            // Add authSource if authentication database is specified
            if (mongoAuthDatabase != null && !mongoAuthDatabase.isEmpty()) {
                uriBuilder.append("?authSource=").append(mongoAuthDatabase);
            } else {
                // Default to using the database itself as auth source
                uriBuilder.append("?authSource=").append(mongoDatabase);
            }

            return uriBuilder.toString();
        }

        // Fallback: URI without credentials
        return String.format("mongodb://%s:%d/%s", mongoHost, mongoPort, mongoDatabase);
    }

    /**
     * Masks password in URI for logging purposes.
     */
    private String maskPassword(String uri) {
        if (uri == null) {
            return null;
        }
        // Replace password with *** in URI
        return uri.replaceAll("://([^:]+):([^@]+)@", "://$1:***@");
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}
