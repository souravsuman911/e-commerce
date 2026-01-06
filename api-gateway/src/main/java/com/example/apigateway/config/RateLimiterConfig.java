package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {
    @Bean(name = "authRateLimiter")
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(20, 40);
    }
}
