package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String uri = exchange.getRequest().getURI().toString();

        logger.info("➡️ Request: {} {}", method, uri);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    int status = exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 0;
                    logger.info("⬅️ Response: {} {} [{}]", method, uri, status);
                }));
    }

    @Override
    public int getOrder() {
        return 0; // Run after CORS and JWT filters
    }
}
