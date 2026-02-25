package com.catalyst.shared.infrastructure.logging;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Kafka Producer Interceptor to propagate the Correlation ID from MDC to Kafka
 * headers.
 */
public class CorrelationIdInterceptor implements ProducerInterceptor<String, String> {

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

        if (correlationId != null) {
            record.headers().add(CorrelationIdFilter.CORRELATION_ID_HEADER,
                    correlationId.getBytes(StandardCharsets.UTF_8));
        }

        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // No action needed
    }

    @Override
    public void close() {
        // No action needed
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // No action needed
    }
}
