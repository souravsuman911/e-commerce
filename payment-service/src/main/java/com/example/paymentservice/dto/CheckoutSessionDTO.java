package com.example.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckoutSessionDTO {
    private String id;

    @JsonProperty("amount_total")
    private Long amountTotal;

    @JsonProperty("payment_status")
    private String paymentStatus;

    private Map<String, String> metadata;
}
