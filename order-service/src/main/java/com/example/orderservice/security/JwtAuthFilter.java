package com.example.orderservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);

            if (jwtUtils.validateJwtToken(token)) {
                String username = jwtUtils.getUsernameFromJwtToken(token);
                List<String> roles = jwtUtils.getRolesFromJwtToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
