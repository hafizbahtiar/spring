package com.hafizbahtiar.spring.config;

import com.hafizbahtiar.spring.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize and @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CorsConfigurationSource corsConfigurationSource;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers("/api/v1/auth/**").permitAll()
                                                .requestMatchers("/api/v1/users").permitAll() // Registration
                                                .requestMatchers("/api/v1/users/by-username/**").permitAll() // Public user lookup
                                                .requestMatchers("/api/v1/subscription-plans/**").permitAll() // Public
                                                                                                              // subscription
                                                                                                              // plans
                                                .requestMatchers("/api/v1/portfolio/public/**").permitAll()
                                                .requestMatchers("/api/v1/portfolio/contacts/public").permitAll() // Public contact form
                                                .requestMatchers("/api/v1/portfolio/testimonials/public").permitAll() // Public
                                                                                                                      // portfolio
                                                                                                                      // view
                                                // File serving endpoints (avatars, blog covers, etc.)
                                                .requestMatchers("/api/v1/files/avatars/**").permitAll()
                                                .requestMatchers("/api/v1/files/blog-covers/**").permitAll()
                                                // Protected endpoints
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
