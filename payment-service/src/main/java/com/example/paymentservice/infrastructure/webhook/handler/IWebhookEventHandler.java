package com.example.paymentservice.infrastructure.webhook.handler;

import com.stripe.model.Event;

public interface IWebhookEventHandler {
    boolean supports(String eventType);
    void handle(Event event) throws Throwable;
}
