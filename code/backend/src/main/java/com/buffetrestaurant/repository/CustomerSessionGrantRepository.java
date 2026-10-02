package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.CustomerSessionGrant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSessionGrantRepository extends JpaRepository<CustomerSessionGrant, Long> {
    Optional<CustomerSessionGrant> findByTokenHash(String tokenHash);
    void deleteByDiningSessionId(Long sessionId);
}
