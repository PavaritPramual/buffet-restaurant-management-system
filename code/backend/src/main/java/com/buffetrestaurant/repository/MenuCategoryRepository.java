package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<MenuCategory> findByArchivedAtIsNull(Sort sort);
    List<MenuCategory> findByArchivedAtIsNotNull(Sort sort);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select category from MenuCategory category where category.id = :id")
    Optional<MenuCategory> findByIdForUpdate(@Param("id") Long id);
}
