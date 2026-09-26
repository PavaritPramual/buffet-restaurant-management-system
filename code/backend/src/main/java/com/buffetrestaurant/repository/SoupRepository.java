package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.Soup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoupRepository extends JpaRepository<Soup, Long> {
    List<Soup> findByActive(boolean active);
}