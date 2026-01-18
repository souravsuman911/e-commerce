package com.example.paymentservice.domain.model;

public class Payment {

    private final Long orderId;
    private PaymentStatus status;
    private final Money amount;

    public Payment(Long orderId, Money amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.CREATED;
    }

    public void markPaid() {
        this.status = PaymentStatus.PAID;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public Long getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Money getAmount() {
        return amount;
    }
}
