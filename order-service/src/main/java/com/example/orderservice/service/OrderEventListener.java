package com.example.orderservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderEventListener(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void listenPaymentEvents(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String orderId = jsonNode.get("orderId").asText();
            String status = jsonNode.get("status").asText();

            System.out.println("Kafka Event Received: orderId=" + orderId + ", status=" + status);

            // Call service to update order status
            orderService.updateOrderStatus(Long.parseLong(orderId), status);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
