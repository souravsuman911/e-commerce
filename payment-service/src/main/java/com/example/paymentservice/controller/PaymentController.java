package com.example.paymentservice.controller;

import com.example.paymentservice.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-checkout-session/{orderId}")
    public ResponseEntity<String> createCheckoutSession(
            @PathVariable Long orderId,
            @RequestParam String email,
            @RequestParam Long amount // in cents
    ) throws StripeException {
        String checkoutUrl = paymentService.createCheckoutSession(orderId, email, amount);
        return ResponseEntity.ok(checkoutUrl);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws JsonProcessingException {
        String result = paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok(result);
    }
}
