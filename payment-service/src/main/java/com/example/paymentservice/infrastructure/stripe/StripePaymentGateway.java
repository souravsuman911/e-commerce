package com.example.paymentservice.infrastructure.stripe;

import com.example.paymentservice.domain.gateway.IPaymentGateway;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Component;

@Component
public class StripePaymentGateway implements IPaymentGateway {

    @Override
    public String createCheckoutSession(Long orderId, String email, Long amount) {
        try {
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setCustomerEmail(email)
                            .setSuccessUrl("http://localhost:3000/success")
                            .setCancelUrl("http://localhost:3000/cancel")
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency("usd")
                                                            .setUnitAmount(amount)
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem
                                                                            .PriceData
                                                                            .ProductData
                                                                            .builder()
                                                                            .setName("Order #" + orderId)
                                                                            .build())
                                                            .build())
                                            .build())
                            .putMetadata("order_id", orderId.toString())
                            .build();

            return Session.create(params).getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Stripe checkout failed", e);
        }
    }
}
