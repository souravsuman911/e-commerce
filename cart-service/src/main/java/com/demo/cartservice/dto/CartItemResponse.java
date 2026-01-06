package com.demo.cartservice.dto;

import lombok.Data;

@Data
public class CartItemResponse {

    private Long id;
    private Long productId;
    private int quantity;

}
