package com.example.paymentservice.infrastructure.kafka;

import com.example.paymentservice.dto.PaymentEventDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentEventPublisher implements IPaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEventDTO> kafkaTemplate;

    public KafkaPaymentEventPublisher(KafkaTemplate<String, PaymentEventDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishSuccess(Long orderId, Double amount) {
        kafkaTemplate.send("payment-events",
                new PaymentEventDTO(orderId, "PAID", amount));
    }

    @Override
    public void publishFailure(Long orderId, Double amount) {
        kafkaTemplate.send("payment-events",
                new PaymentEventDTO(orderId, "FAILED", amount));
    }
}
