package com.demo.cartservice.repository;

import com.demo.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
