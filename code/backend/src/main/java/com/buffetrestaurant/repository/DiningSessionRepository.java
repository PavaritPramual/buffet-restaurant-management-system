package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiningSessionRepository extends JpaRepository<DiningSession, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select diningSession from DiningSession diningSession where diningSession.sessionToken = :token")
    Optional<DiningSession> findBySessionTokenForUpdate(@Param("token") String token);

    boolean existsByRestaurantTableId(Long tableId);
    boolean existsByRestaurantTableIdAndStatus(Long tableId, DiningSessionStatus status);

    java.util.List<DiningSession> findByStatusOrderByStartTimeDesc(DiningSessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select diningSession from DiningSession diningSession where diningSession.id = :id")
    Optional<DiningSession> findByIdForUpdate(@Param("id") Long id);
}
