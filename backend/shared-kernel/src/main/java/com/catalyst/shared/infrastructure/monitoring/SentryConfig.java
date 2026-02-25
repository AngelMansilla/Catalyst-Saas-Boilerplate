package com.catalyst.shared.infrastructure.monitoring;

import io.sentry.SentryOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.catalyst.shared.infrastructure.logging.CorrelationIdFilter.CORRELATION_ID_MDC_KEY;

/**
 * Configuration for Sentry error tracking.
 * Automatically attaches the Correlation ID from SLF4J MDC to every event sent
 * to Sentry.
 */
@Configuration
public class SentryConfig {

    private static final Logger log = LoggerFactory.getLogger(SentryConfig.class);

    @PostConstruct
    public void init() {
        log.info("Sentry configuration initialized - Hub active: {}", io.sentry.Sentry.isEnabled());
    }

    @Bean
    public SentryOptions.BeforeSendCallback beforeSendCallback() {
        return (event, hint) -> {
            String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
            if (correlationId != null) {
                event.setTag("correlation_id", correlationId);
            }
            return event;
        };
    }
}
