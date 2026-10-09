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
    List<StockItem> findAllByOrderByNameAsc();
    List<StockItem> findAllByArchivedAtIsNullOrderByNameAsc();
    List<StockItem> findAllByArchivedAtIsNotNullOrderByNameAsc();
    Optional<StockItem> findBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from StockItem item where item.id = :id")
    Optional<StockItem> findByIdForUpdate(@Param("id") Long id);
}