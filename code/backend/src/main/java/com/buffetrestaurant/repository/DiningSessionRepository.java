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
    Optional<DiningSession> findBySessionTokenAndStatus(String sessionToken, DiningSessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select diningSession from DiningSession diningSession where diningSession.id = :id")
    Optional<DiningSession> findByIdForUpdate(@Param("id") Long id);
}
