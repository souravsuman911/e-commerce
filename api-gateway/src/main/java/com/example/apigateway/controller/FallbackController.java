package com.example.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public ResponseEntity<Map<String, Object>> handleFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Service temporarily unavailable. Please try again later.");
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
