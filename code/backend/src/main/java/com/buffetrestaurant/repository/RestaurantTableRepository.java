package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.enums.TableStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByStatus(TableStatus status);

    Optional<RestaurantTable> findByTableNumber(String tableNumber);

    boolean existsByTableNumber(String tableNumber);

    boolean existsByTableNumberAndIdNot(String tableNumber, Long id);
}
