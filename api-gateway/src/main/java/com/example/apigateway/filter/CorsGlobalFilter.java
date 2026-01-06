package com.example.apigateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorsGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.add("Access-Control-Allow-Origin", "http://localhost:3000");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "*");
        headers.add("Access-Control-Allow-Credentials", "true");

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -2; // Run before JWT filter
    }
}
