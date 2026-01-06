package com.demo.cartservice.service;

import com.demo.cartservice.dto.AddItemRequest;
import com.demo.cartservice.dto.CartItemResponse;
import com.demo.cartservice.dto.CartResponse;
import com.demo.cartservice.entity.Cart;
import com.demo.cartservice.entity.CartItem;
import com.demo.cartservice.repository.CartItemRepository;
import com.demo.cartservice.repository.CartRepository;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.restTemplate = restTemplate;
    }

    private Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepository.save(cart);
        });
    }

    private ProductResponse fetchProduct(Long productId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token); // Forward token

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "http://PRODUCT-SERVICE/api/products/" + productId, // direct call
                HttpMethod.GET,
                entity,
                ProductResponse.class
        );

        return response.getBody();
    }

    public CartResponse addItemToCart(Long userId, AddItemRequest request, String token) {
        ProductResponse product = fetchProduct(request.getProductId(), token);

        if (product == null) {
            throw new RuntimeException("Product not found with ID: " + request.getProductId());
        }
        else if(request.getQuantity() > product.getStock()){
            throw new RuntimeException("You can only add " + product.getStock() + " number of items for the product id " + request.getProductId());
        }

        // 2. Find or create cart
        Cart cart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUserId(userId);
                        return cartRepository.save(newCart);
                    });

        // 3. Add item
        CartItem item = new CartItem();
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setCart(cart);

        cart.getItems().add(item);

        return convertToDto(cartRepository.save(cart));
    }

    public CartResponse getCartResponse(Long userId) {
        Cart cart = getCartByUserId(userId);
        return convertToDto(cart);
    }

    public void removeItem(Long itemId) {
        cartItemRepository.deleteById(itemId);
    }

    private CartResponse convertToDto(Cart cart) {
        CartResponse dto = new CartResponse();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());

        List<CartItemResponse> itemDTOs = cart.getItems().stream().map(item -> {
            CartItemResponse cir = new CartItemResponse();
            cir.setId(item.getId());
            cir.setProductId(item.getProductId());
            cir.setQuantity(item.getQuantity());
            return cir;
        }).collect(Collectors.toList());

        dto.setItems(itemDTOs);

        return dto;
    }

    @Data
    private static class ProductResponse {
        private Double price;
        private Integer stock;
    }

    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("Cart not found for user " + userId));
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
