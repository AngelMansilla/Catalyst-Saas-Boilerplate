package com.catalyst.payment.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Payment Service.
 * Allows public access to certain endpoints for testing and development.
 */
@Configuration
@EnableWebSecurity
public class PaymentSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain paymentSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/payments/**", "/api/v1/webhooks/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitting all for now to facilitate testing the Kafka/Notification flow
                        .requestMatchers("/api/v1/payments/subscriptions/checkout").permitAll()
                        .requestMatchers("/api/v1/webhooks/stripe").permitAll()
                        // Other payment endpoints might still need authentication in a real scenario
                        .anyRequest().authenticated());

        return http.build();
    }
}
