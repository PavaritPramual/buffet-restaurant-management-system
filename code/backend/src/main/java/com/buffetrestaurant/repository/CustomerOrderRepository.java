package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.CustomerOrder;
import com.buffetrestaurant.domain.enums.OrderStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    // Lock the root row, without a nullable collection join in PostgreSQL FOR UPDATE.
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select o from CustomerOrder o where o.id = :id")
    Optional<CustomerOrder> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findBySessionIdOrderByCreatedAtDesc(Long sessionId);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findByIdAndSessionId(Long id, Long sessionId);

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses);

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    @Override
    @EntityGraph(attributePaths = "items")
    java.util.Optional<CustomerOrder> findById(Long id);
}
