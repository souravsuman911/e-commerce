package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.security.JwtUtils;
import com.example.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;
    private final JwtUtils jwtUtils;

    public OrderController(OrderService service, JwtUtils jwtUtils) {
        this.service = service;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        return ResponseEntity.ok(service.placeOrder(userId, token));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        return ResponseEntity.ok(service.getOrdersByUser(userId));
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId,
                                                  @RequestParam String status) {
        service.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }


    private Long getUserIdFromToken(String headerToken) {
        String token = headerToken.replace("Bearer ", "");
        return Long.valueOf(jwtUtils.getUserIdFromJwtToken(token));
    }
}
