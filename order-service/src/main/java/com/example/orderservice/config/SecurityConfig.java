package com.example.orderservice.config;

import com.example.orderservice.security.JwtAuthFilter;
import com.example.orderservice.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/orders/update/**").permitAll()
                        .requestMatchers("/api/orders/**").hasRole("USER")  // only logged-in users can modify cart
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(new JwtAuthFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
