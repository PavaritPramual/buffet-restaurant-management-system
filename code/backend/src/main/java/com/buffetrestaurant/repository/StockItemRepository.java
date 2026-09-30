package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.StockItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    List<StockItem> findAllByOrderByNameAsc();
    Optional<StockItem> findBySku(String sku);
}