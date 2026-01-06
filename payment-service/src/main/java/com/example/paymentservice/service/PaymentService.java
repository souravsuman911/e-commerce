package com.example.paymentservice.service;

import com.example.paymentservice.dto.CheckoutSessionDTO;
import com.example.paymentservice.dto.PaymentEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${payment.event}")
    private String PAYMENT_TOPIC;

    private final RestTemplate restTemplate;

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentService(RestTemplate restTemplate, KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    private String token;

    /**
     * Create a Stripe Checkout Session
     */
    public String createCheckoutSession(Long orderId, String customerEmail, Long amount) throws StripeException {
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setCustomerEmail(customerEmail)
                        .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl("http://localhost:3000/cancel")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("usd")
                                                        .setUnitAmount(amount) // in cents
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Order #" + orderId)
                                                                        .build())
                                                        .build())
                                        .build())
                        .putMetadata("order_id", orderId.toString())
                        .build();

        Session session = Session.create(params);
        return session.getUrl(); // return the Checkout URL
    }

    /**
     * Handle Stripe webhook events
     */
    public String handleWebhook(String payload, String sigHeader) throws JsonProcessingException {
        Event event;

        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );
        } catch (SignatureVerificationException e) {
            return "Webhook signature verification failed.";
        }

        // Deserialize event object
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                String rawJson = event.getDataObjectDeserializer().getRawJson();
                CheckoutSessionDTO sessionDTO = objectMapper.readValue(rawJson, CheckoutSessionDTO.class);
                System.out.println("Checkout completed for session: " + sessionDTO.getId());

//                // Synchronous call to order-service to update the order status(PAID)
//                if (sessionDTO.getMetadata() != null) {
//                    String orderId = sessionDTO.getMetadata().get("order_id");
//                    markOrderAsPaid(orderId, "PAID");
//                }

                Long orderId = Long.valueOf(sessionDTO.getMetadata().get("order_id"));
                Double amount = Double.valueOf(sessionDTO.getAmountTotal());

                if("paid".equalsIgnoreCase(sessionDTO.getPaymentStatus())) {
                    publishPaymentSuccess(orderId, amount);
                }
                else {
                    publishPaymentFailed(orderId, amount);
                }
            }
            case "payment_intent.succeeded" -> {
                PaymentIntent paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().orElse(null);
                if (paymentIntent != null) {
                    System.out.println("Payment succeeded: " + paymentIntent.getId());
                }
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().orElse(null);
                if (paymentIntent != null) {
                    System.out.println("Payment failed: " + paymentIntent.getLastPaymentError().getMessage());
                }
            }
            default -> System.out.println("Unhandled event type: " + event.getType());
        }

        return "Webhook handled";
    }

    /**
     * Simulate updating order status in DB
     */
    private void markOrderAsPaid(String orderId, String status) {
        String url = orderServiceUrl + "/update/" + orderId + "?status=" + status;
        String token = fetchServiceToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Order " + orderId + " updated successfully to status: " + status);
            } else {
                System.err.println("Failed to update order " + orderId + ". Response code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error while updating order: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Order " + orderId + " marked as PAID in DB.");
    }

    // Call this method when payment succeeds (webhook)
    private void publishPaymentSuccess(Long orderId, Double amount) {
        PaymentEvent event = new PaymentEvent(orderId, "PAID", amount);
        kafkaTemplate.send(PAYMENT_TOPIC, event);
        System.out.println("Published payment success event to Kafka: " + event);
    }

    // Call this method if payment fails
    private void publishPaymentFailed(Long orderId, Double amount) {
        PaymentEvent event = new PaymentEvent(orderId, "FAILED", amount);
        kafkaTemplate.send(PAYMENT_TOPIC, event);
        System.out.println("Published payment failed event to Kafka: " + event);
    }

    private String fetchServiceToken() {
        Map<String, String> request = new HashMap<>();
        request.put("clientId", "payment-service");
        request.put("clientSecret", "secret123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                authServiceUrl + "/token",
                request,
                Map.class
        );

        if (response.getBody() != null && response.getBody().get("token") != null) {
            return (String) response.getBody().get("token");
        }
        throw new RuntimeException("Failed to fetch service token");
    }
}
