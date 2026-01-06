package com.demo.productservice.config;

import com.demo.productservice.security.JwtAuthFilter;
import com.demo.productservice.security.JwtUtils;
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
                        .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/products/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(new JwtAuthFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
