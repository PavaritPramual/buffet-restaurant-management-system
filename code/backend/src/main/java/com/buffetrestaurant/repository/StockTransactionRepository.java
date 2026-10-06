package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.StockTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    boolean existsByStockItemId(Long stockItemId);
    List<StockTransaction> findAllByOrderByCreatedAtDescIdDesc();
    List<StockTransaction> findAllByStockItemIdOrderByCreatedAtDescIdDesc(Long stockItemId);
}