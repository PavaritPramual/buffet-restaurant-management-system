package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.enums.TableStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByStatus(TableStatus status);

    Optional<RestaurantTable> findByTableNumber(String tableNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select restaurantTable from RestaurantTable restaurantTable where restaurantTable.id = :id")
    Optional<RestaurantTable> findByIdForUpdate(Long id);

    boolean existsByTableNumber(String tableNumber);

    boolean existsByTableNumberAndIdNot(String tableNumber, Long id);
}
