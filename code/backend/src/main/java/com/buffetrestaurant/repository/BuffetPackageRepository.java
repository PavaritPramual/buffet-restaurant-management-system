package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.BuffetPackage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuffetPackageRepository extends JpaRepository<BuffetPackage, Long> {
    List<BuffetPackage> findByActive(boolean active);
    List<BuffetPackage> findByArchived(boolean archived);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from BuffetPackage p where p.id = :id")
    java.util.Optional<BuffetPackage> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query(value = "select count(*) > 0 from package_menu_items where package_id = :id", nativeQuery = true)
    boolean existsMenuLinks(@org.springframework.data.repository.query.Param("id") Long id);
}
