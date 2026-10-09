package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.ManagerOperation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerOperationRepository extends JpaRepository<ManagerOperation, Long> {
    List<ManagerOperation> findTop50ByOrderByIdDesc();
}
