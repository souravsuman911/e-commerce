package com.example.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private Long orderId;
    private String status; // PAID, FAILED
    private Double amount;
}
