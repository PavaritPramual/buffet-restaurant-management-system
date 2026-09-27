package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category", "packageIds"})
    @Query("select distinct item from MenuItem item join item.packageIds packageId "
            + "where item.available = true and packageId = :packageId order by item.category.name, item.name")
    List<MenuItem> findAvailableForPackage(@Param("packageId") Long packageId);
}
