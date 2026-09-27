package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.BuffetPackage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuffetPackageRepository extends JpaRepository<BuffetPackage, Long> {
    List<BuffetPackage> findByActive(boolean active);
}