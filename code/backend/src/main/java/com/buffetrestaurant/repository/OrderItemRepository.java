package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByMenuItemId(Long menuItemId);
}
