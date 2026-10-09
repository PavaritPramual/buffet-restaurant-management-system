package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);
    boolean existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull(String name, Long id);
    boolean existsByCategoryId(Long categoryId);

    org.springframework.data.domain.Page<MenuItem> findByDeletedAtIsNull(org.springframework.data.domain.Pageable pageable);
    java.util.Optional<MenuItem> findByIdAndDeletedAtIsNull(Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from MenuItem item where item.id = :id and item.deletedAt is null")
    java.util.Optional<MenuItem> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "packageIds"})
    @Query("select distinct item from MenuItem item join item.packageIds packageId "
            + "where item.available = true and item.deletedAt is null and packageId = :packageId order by item.category.name, item.name")
    List<MenuItem> findAvailableForPackage(@Param("packageId") Long packageId);
}
