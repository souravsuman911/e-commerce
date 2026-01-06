package com.example.orderservice.entity;

import com.example.orderservice.entity.OrderItem;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
}
