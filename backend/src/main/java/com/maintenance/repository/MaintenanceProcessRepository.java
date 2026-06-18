package com.maintenance.repository;

import com.maintenance.entity.MaintenanceProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceProcessRepository extends JpaRepository<MaintenanceProcess, Long> {

    List<MaintenanceProcess> findByCategoryIdOrderBySortOrderAsc(Long categoryId);

    Optional<MaintenanceProcess> findByProcessCode(String processCode);
}
