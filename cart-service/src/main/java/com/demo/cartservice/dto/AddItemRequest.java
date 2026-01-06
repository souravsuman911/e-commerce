package com.demo.cartservice.dto;

import lombok.Data;

@Data
public class AddItemRequest {

    private Long productId;
    private Integer quantity;

}
