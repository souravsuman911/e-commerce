package com.example.paymentservice.controller;

import com.example.paymentservice.application.PaymentService;
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

    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<String> checkout(
            @PathVariable Long orderId,
            @RequestParam String email,
            @RequestParam Long amount
    ) throws StripeException {
        return ResponseEntity.ok(
                paymentService.createCheckout(orderId, email, amount)
        );
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("Webhook processed");
    }
}
