package com.maintenance.repository;

import com.maintenance.entity.MaintenanceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, Long> {

    Page<MaintenanceOrder> findByCustomerId(Long customerId, Pageable pageable);

    Page<MaintenanceOrder> findByEquipmentId(Long equipmentId, Pageable pageable);

    List<MaintenanceOrder> findByPlanDateBetween(LocalDate startDate, LocalDate endDate);

    List<MaintenanceOrder> findByActualDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT m FROM MaintenanceOrder m WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "m.orderNo LIKE %:keyword% OR " +
           "m.engineer LIKE %:keyword%) AND " +
           "(:customerId IS NULL OR m.customerId = :customerId) AND " +
           "(:equipmentId IS NULL OR m.equipmentId = :equipmentId) AND " +
           "(:orderStatus IS NULL OR :orderStatus = '' OR m.orderStatus = :orderStatus) AND " +
           "(:orderType IS NULL OR :orderType = '' OR m.orderType = :orderType) AND " +
           "(:startDate IS NULL OR m.planDate >= :startDate) AND " +
           "(:endDate IS NULL OR m.planDate <= :endDate)")
    Page<MaintenanceOrder> findByConditions(
            @Param("keyword") String keyword,
            @Param("customerId") Long customerId,
            @Param("equipmentId") Long equipmentId,
            @Param("orderStatus") String orderStatus,
            @Param("orderType") String orderType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
