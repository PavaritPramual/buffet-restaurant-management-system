package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiningSessionRepository extends JpaRepository<DiningSession, Long> {
    Optional<DiningSession> findBySessionTokenAndStatus(String sessionToken, DiningSessionStatus status);
}
