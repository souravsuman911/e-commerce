package com.demo.cartservice.controller;

import com.demo.cartservice.dto.AddItemRequest;
import com.demo.cartservice.dto.CartResponse;
import com.demo.cartservice.security.JwtUtils;
import com.demo.cartservice.service.CartService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;
    private final JwtUtils jwtUtils;

    public CartController(CartService service, JwtUtils jwtUtils) {
        this.service = service;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/add")
    public CartResponse addItem(@RequestHeader("Authorization") String headerToken,
                                @RequestBody AddItemRequest request) {
        Long userId = getUserIdFromToken(headerToken);
        return service.addItemToCart(userId, request, headerToken);
    }

    @GetMapping
    public CartResponse getCart(@RequestHeader("Authorization") String headerToken) {
        Long userId = getUserIdFromToken(headerToken);
        return service.getCartResponse(userId);
    }

    @DeleteMapping("/remove/{itemId}")
    public void removeItem(@PathVariable Long itemId) {
        service.removeItem(itemId);
    }

    @DeleteMapping("/clear")
    public void clearCart(@RequestHeader("Authorization") String headerToken){
        service.clearCart(getUserIdFromToken(headerToken));
    }

    private Long getUserIdFromToken(String headerToken) {
        String token = headerToken.replace("Bearer ", "");
        return Long.valueOf(jwtUtils.getUserIdFromJwtToken(token));
    }

}
