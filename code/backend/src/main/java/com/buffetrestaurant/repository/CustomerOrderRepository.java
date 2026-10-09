package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.CustomerOrder;
import com.buffetrestaurant.domain.enums.OrderStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findBySessionIdOrderByCreatedAtDesc(Long sessionId);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findByIdAndSessionId(Long id, Long sessionId);

    @EntityGraph(attributePaths = "items")
    @org.springframework.data.jpa.repository.Query("select o from CustomerOrder o where o.status in :statuses "
            + "and not exists (select s.id from DiningSession s where s.id = o.sessionId "
            + "and s.status = com.buffetrestaurant.domain.enums.DiningSessionStatus.CANCELLED) order by o.createdAt asc")
    List<CustomerOrder> findByStatusInOrderByCreatedAtAsc(@org.springframework.data.repository.query.Param("statuses") Collection<OrderStatus> statuses);

    @EntityGraph(attributePaths = "items")
    @org.springframework.data.jpa.repository.Query("select o from CustomerOrder o where o.status = :status "
            + "and not exists (select s.id from DiningSession s where s.id = o.sessionId "
            + "and s.status = com.buffetrestaurant.domain.enums.DiningSessionStatus.CANCELLED) order by o.createdAt asc")
    List<CustomerOrder> findByStatusOrderByCreatedAtAsc(@org.springframework.data.repository.query.Param("status") OrderStatus status);

    @Override
    @EntityGraph(attributePaths = "items")
    java.util.Optional<CustomerOrder> findById(Long id);
}
