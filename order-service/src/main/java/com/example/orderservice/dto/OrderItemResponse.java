package com.example.orderservice.dto;

import lombok.Data;

@Data
public class OrderItemResponse {

    private Long productId;
    private int quantity;
    private Double price;

}
