package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public RedisRateLimiter rateLimiter(){
        return new RedisRateLimiter(5, 10);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(rateLimiter());
                            config.setKeyResolver(ipKeyResolver());
                        }))
                        .uri("lb://AUTH-SERVICE"))

                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(rateLimiter());
                            config.setKeyResolver(ipKeyResolver());
                        }))
                        .uri("lb://PRODUCT-SERVICE"))

                .route("cart-service", r -> r.path("/api/cart/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(rateLimiter());
                            config.setKeyResolver(ipKeyResolver());
                        }))
                        .uri("lb://CART-SERVICE"))

                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(rateLimiter());
                            config.setKeyResolver(ipKeyResolver());
                        }))
                        .uri("lb://ORDER-SERVICE"))

                .route("payment-service", r -> r.path("/api/payments/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(rateLimiter());
                            config.setKeyResolver(ipKeyResolver());
                        }))
                        .uri("lb://PAYMENT-SERVICE"))
                .build();
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        // Use client IP as rate-limiting key
        return exchange ->
                Mono.just(exchange.getRequest()
                    .getRemoteAddress()
                    .getAddress()
                    .getHostAddress());
    }
}
