package com.example.paymentservice.service;

import com.example.paymentservice.domain.gateway.IPaymentGateway;
import com.example.paymentservice.infrastructure.webhook.WebhookProcessor;
import com.stripe.exception.StripeException;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final IPaymentGateway paymentGateway;
    private final WebhookProcessor webhookProcessor;

    public PaymentService(
            IPaymentGateway paymentGateway,
            WebhookProcessor webhookProcessor
    ) {
        this.paymentGateway = paymentGateway;
        this.webhookProcessor = webhookProcessor;
    }

    public String createCheckout(Long orderId, String email, Long amount)
            throws StripeException {
        return paymentGateway.createCheckoutSession(orderId, email, amount);
    }

    public void handleWebhook(String payload, String signature) {
        webhookProcessor.process(payload, signature);
    }
}
