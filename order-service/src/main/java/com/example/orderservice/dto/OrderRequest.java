package com.example.orderservice.dto;

import lombok.Data;

@Data
public class OrderRequest {

    private Long userId; // extracted from JWT

}
