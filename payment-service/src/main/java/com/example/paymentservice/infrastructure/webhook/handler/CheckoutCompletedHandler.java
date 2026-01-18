package com.example.paymentservice.infrastructure.webhook.handler;

import com.example.paymentservice.dto.CheckoutSessionDTO;
import com.example.paymentservice.exception.WebhookProcessingException;
import com.example.paymentservice.infrastructure.kafka.IPaymentEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import org.springframework.stereotype.Component;

@Component
public class CheckoutCompletedHandler implements IWebhookEventHandler {

    private final IPaymentEventPublisher publisher;

    public CheckoutCompletedHandler(IPaymentEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public boolean supports(String eventType) {
        return "checkout.session.completed".equals(eventType);
    }

    @Override
    public void handle(Event event) throws Throwable {
        try {
            String rawJson = event.getDataObjectDeserializer().getRawJson();

            CheckoutSessionDTO session =
                    new ObjectMapper().readValue(rawJson, CheckoutSessionDTO.class);

            Long orderId = Long.valueOf(session.getMetadata().get("order_id"));
            Double amount = session.getAmountTotal() / 100.0;

            if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
                publisher.publishSuccess(orderId, amount);
            } else {
                publisher.publishFailure(orderId, amount);
            }

        } catch (Exception e) {
            throw new Throwable("Failed to process checkout session", e);
        }
    }
}
