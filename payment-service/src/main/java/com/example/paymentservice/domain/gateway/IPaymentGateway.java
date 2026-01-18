package com.example.paymentservice.domain.gateway;

import com.stripe.exception.StripeException;

public interface IPaymentGateway {
    String createCheckoutSession(Long orderId, String email, Long amount)
            throws StripeException;
}
