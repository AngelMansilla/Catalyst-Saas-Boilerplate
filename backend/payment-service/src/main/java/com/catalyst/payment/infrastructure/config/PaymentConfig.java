package com.catalyst.payment.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Payment service configuration.
 */
@Configuration
@EnableScheduling
public class PaymentConfig {

    @Value("${payment.trial.duration-days:14}")
    private int trialDurationDays;

    /**
     * Provides the trial duration days for injection.
     *
     * @return the number of trial days
     */
    @Bean
    public int trialDurationDays() {
        return trialDurationDays;
    }

    /**
     * Configures ObjectMapper with Java 8 time module.
     *
     * @return configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}

