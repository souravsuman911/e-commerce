package com.example.paymentservice.infrastructure.webhook;

import com.example.paymentservice.infrastructure.webhook.handler.IWebhookEventHandler;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebhookProcessor {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final List<IWebhookEventHandler> handlers;

    public WebhookProcessor(List<IWebhookEventHandler> handlers) {
        this.handlers = handlers;
    }

    public void process(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, endpointSecret);

            handlers.stream()
                    .filter(h -> h.supports(event.getType()))
                    .findFirst()
                    .ifPresent(h -> {
                        try {
                            h.handle(event);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("Failed to process checkout session", e);
        }
    }
}
