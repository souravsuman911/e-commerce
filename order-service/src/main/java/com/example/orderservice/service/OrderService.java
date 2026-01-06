package com.example.orderservice.service;

import com.example.orderservice.dto.OrderItemResponse;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.repository.OrderItemRepository;
import com.example.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final RestTemplate restTemplate;

    public OrderService(OrderRepository orderRepo, OrderItemRepository orderItemRepo, RestTemplate restTemplate) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.restTemplate = restTemplate;
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

    private CartResponse fetchCart(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                "http://CART-SERVICE/api/cart",
                HttpMethod.GET,
                entity,
                CartResponse.class
        ).getBody();
    }

    private void clearCart(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange("http://CART-SERVICE/api/cart/clear",
                        HttpMethod.DELETE,
                        entity,
                        Void.class);
    }

    public OrderResponse placeOrder(Long userId, String token) {
        List<CartItemResponse> cartItems = fetchCart(token).getItems();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty!");
        }

        // 2. Fetch product details & calculate total
        double total = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemResponse c : cartItems) {
            ProductResponse product = fetchProduct(c.getProductId(), token);

            double price = product.getPrice();
            total += price * c.getQuantity();

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(c.getProductId());
            orderItem.setQuantity(c.getQuantity());
            orderItem.setPrice(price);
            orderItems.add(orderItem);
        }

        // 3. Save order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setItems(orderItems);

        orderItems.forEach(i -> i.setOrder(order));
        orderRepo.save(order);

        // 4. Clear cart
        clearCart(token);

        // 5. Convert to DTO
        return convertToResponse(order);
    }

    // Method to get all orders of a user
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepo.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> items = order.getItems().stream().map(i -> {
            OrderItemResponse cir = new OrderItemResponse();
            cir.setProductId(i.getProductId());
            cir.setQuantity(i.getQuantity());
            cir.setPrice(i.getPrice());
            return cir;
        }).collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        orderRepo.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepo.save(order);
        });
    }


    @Data
    private static class CartResponse {
        private Long id;
        private Long userId;
        private List<CartItemResponse> items;
    }

    // placeholder DTOs for external responses
    @Data
    private static class CartItemResponse {
        private Long id;
        private Long productId;
        private Integer quantity;
    }

    private static class ProductResponse {
        private Double price;
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
}
