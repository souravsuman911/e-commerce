package com.demo.authservice.dto;

import lombok.Data;

@Data
public class TokenRequest {
    private String clientId;
    private String clientSecret;
}
