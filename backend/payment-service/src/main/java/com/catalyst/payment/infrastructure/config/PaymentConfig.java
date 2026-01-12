package com.catalyst.payment.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Payment service configuration.
 */
@Configuration
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
}

