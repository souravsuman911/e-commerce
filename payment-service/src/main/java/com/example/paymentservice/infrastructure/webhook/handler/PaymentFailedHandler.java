package com.example.paymentservice.infrastructure.webhook.handler;

import com.example.paymentservice.infrastructure.kafka.IPaymentEventPublisher;
import com.stripe.model.Event;
import org.springframework.stereotype.Component;

@Component
public class PaymentFailedHandler implements IWebhookEventHandler {

    private final IPaymentEventPublisher publisher;

    public PaymentFailedHandler(IPaymentEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public boolean supports(String eventType) {
        return "payment_intent.payment_failed".equals(eventType);
    }

    @Override
    public void handle(Event event) {
        // optional: extract orderId
        publisher.publishFailure(-1L, 0.0);
    }
}
