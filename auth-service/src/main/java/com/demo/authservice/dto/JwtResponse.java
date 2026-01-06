package com.demo.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;

    private String type = "bearer";

    private Long id;

    private String username;

    private String email;

    private List<String> roles;
}
