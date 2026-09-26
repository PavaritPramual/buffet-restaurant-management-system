package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.CustomerOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findBySessionIdOrderByCreatedAtDesc(Long sessionId);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findByIdAndSessionId(Long id, Long sessionId);

    @Override
    @EntityGraph(attributePaths = "items")
    java.util.Optional<CustomerOrder> findById(Long id);
}
