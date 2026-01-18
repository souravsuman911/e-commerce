package com.example.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentEventDTO {
    private Long orderId;
    private String status;
    private Double amount;
}
