package com.demo.cartservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "carts")
public class Cart {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List < CartItem > items;

}