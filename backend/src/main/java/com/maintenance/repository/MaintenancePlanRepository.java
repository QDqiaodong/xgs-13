package com.maintenance.repository;

import com.maintenance.entity.MaintenancePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, Long> {

    Page<MaintenancePlan> findByEquipmentId(Long equipmentId, Pageable pageable);

    List<MaintenancePlan> findByPlanDateBetweenAndPlanStatus(LocalDate startDate, LocalDate endDate, String status);

    long countByEquipmentId(Long equipmentId);

    @Query("SELECT m FROM MaintenancePlan m WHERE " +
           "(:equipmentId IS NULL OR m.equipmentId = :equipmentId) AND " +
           "(:planStatus IS NULL OR :planStatus = '' OR m.planStatus = :planStatus) AND " +
           "(:startDate IS NULL OR m.planDate >= :startDate) AND " +
           "(:endDate IS NULL OR m.planDate <= :endDate)")
    Page<MaintenancePlan> findByConditions(
            @Param("equipmentId") Long equipmentId,
            @Param("planStatus") String planStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
