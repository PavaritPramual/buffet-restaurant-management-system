package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByCategoryIdAndArchivedAtIsNull(Long categoryId);
    Page<MenuItem> findByArchivedAtIsNullAndCategoryArchivedAtIsNull(Pageable pageable);
    Page<MenuItem> findByArchivedAtIsNotNull(Pageable pageable);
    @Query("select item.category.id from MenuItem item where item.id = :id")
    Optional<Long> findCategoryId(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from MenuItem item where item.id = :id")
    Optional<MenuItem> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "packageIds"})
    @Query("select distinct item from MenuItem item join item.packageIds packageId "
            + "where item.available = true and item.archivedAt is null and item.category.archivedAt is null "
            + "and packageId = :packageId order by item.category.name, item.name")
    List<MenuItem> findAvailableForPackage(@Param("packageId") Long packageId);
}
