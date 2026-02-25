package com.catalyst.shared.infrastructure.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that extracts or generates a Correlation ID for each request.
 * The ID is stored in the SLF4J MDC (Mapped Diagnostic Context) so it can be
 * included in all logs generated during the request execution.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);

            if (!StringUtils.hasText(correlationId)) {
                correlationId = UUID.randomUUID().toString();
            }

            try {
                MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
                httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
                chain.doFilter(request, response);
            } finally {
                MDC.remove(CORRELATION_ID_MDC_KEY);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
