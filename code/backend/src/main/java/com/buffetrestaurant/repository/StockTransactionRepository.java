package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.StockTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    boolean existsByStockItemId(Long stockItemId);

    @Query("select count(t) > 0 from StockTransaction t where t.actor.id = :userId")
    boolean existsByActorId(@Param("userId") Long userId);
    List<StockTransaction> findAllByOrderByCreatedAtDescIdDesc();
    List<StockTransaction> findAllByStockItemIdOrderByCreatedAtDescIdDesc(Long stockItemId);
}