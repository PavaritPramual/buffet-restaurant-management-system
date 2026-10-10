package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.Soup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoupRepository extends JpaRepository<Soup, Long> {
    List<Soup> findByActive(boolean active);
    List<Soup> findByArchived(boolean archived);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from Soup s where s.id = :id")
    java.util.Optional<Soup> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
