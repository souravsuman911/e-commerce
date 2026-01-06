package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private int quantity;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
