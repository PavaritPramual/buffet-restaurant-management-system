package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.StockItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    @Query(value = "SELECT (SELECT COUNT(*) FROM menu_stock_usage WHERE stock_item_id = :id) + (SELECT COUNT(*) FROM order_item_stock_usage WHERE stock_item_id = :id)", nativeQuery = true)
    long countRecipeReferences(@Param("id") Long id);
    List<StockItem> findAllByOrderByNameAsc();
    List<StockItem> findAllByArchivedAtIsNullOrderByNameAsc();
    List<StockItem> findAllByArchivedAtIsNotNullOrderByNameAsc();
    Optional<StockItem> findBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from StockItem item where item.id = :id")
    Optional<StockItem> findByIdForUpdate(@Param("id") Long id);
}
