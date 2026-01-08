package com.hafizbahtiar.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for file storage and serving static files.
 * Configures resource handlers for serving uploaded files (avatars, etc.).
 */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${app.file-storage.upload-dir:uploads/avatars}")
    private String avatarUploadDir;

    @Value("${app.file-storage.base-url:http://localhost:8080/api/v1/files/avatars}")
    private String avatarBaseUrl;

    @Value("${app.file-storage.blog-covers.upload-dir:uploads/blog-covers}")
    private String blogCoversUploadDir;

    @Value("${app.file-storage.blog-covers.base-url:http://localhost:8080/api/v1/files/blog-covers}")
    private String blogCoversBaseUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve avatar files
        Path avatarUploadPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        String avatarUploadPathStr = avatarUploadPath.toFile().getAbsolutePath();

        registry.addResourceHandler("/api/v1/files/avatars/**")
                .addResourceLocations("file:" + avatarUploadPathStr + "/")
                .setCachePeriod(3600); // Cache for 1 hour

        // Serve blog cover images
        Path blogCoversUploadPath = Paths.get(blogCoversUploadDir).toAbsolutePath().normalize();
        String blogCoversUploadPathStr = blogCoversUploadPath.toFile().getAbsolutePath();

        registry.addResourceHandler("/api/v1/files/blog-covers/**")
                .addResourceLocations("file:" + blogCoversUploadPathStr + "/")
                .setCachePeriod(3600); // Cache for 1 hour
    }
}
