package com.example.orderservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    private Long id;
    private Long userId;
    private Double totalAmount;
    private String status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

}
