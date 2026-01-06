package com.example.apigateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", exchange.getRequest().getPath().toString());
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        // Determine proper HTTP status
        if (ex.getMessage() != null && ex.getMessage().contains("Unauthorized")) {
            errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        } else if (ex.getMessage() != null && ex.getMessage().contains("Rate limit exceeded")) {
            errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        }

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception e) {
            bytes = ("{\"error\":\"Unexpected error\"}").getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponse().setStatusCode(HttpStatus.valueOf((int) errorResponse.get("status")));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
