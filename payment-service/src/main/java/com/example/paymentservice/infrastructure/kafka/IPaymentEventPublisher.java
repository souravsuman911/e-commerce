package com.example.paymentservice.infrastructure.kafka;

public interface IPaymentEventPublisher {
    void publishSuccess(Long orderId, Double amount);
    void publishFailure(Long orderId, Double amount);
}
