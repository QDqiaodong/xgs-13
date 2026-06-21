package com.maintenance.repository;

import com.maintenance.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Page<Equipment> findByCustomerId(Long customerId, Pageable pageable);

    Page<Equipment> findByCategoryId(Long categoryId, Pageable pageable);

    List<Equipment> findByNextMaintenanceDateBefore(LocalDate date);

    long countByCustomerId(Long customerId);

    long countByCategoryId(Long categoryId);

    @Query("SELECT e FROM Equipment e WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "e.equipmentModel LIKE %:keyword% OR " +
           "e.serialNumber LIKE %:keyword% OR " +
           "e.installAddress LIKE %:keyword%) AND " +
           "(:customerId IS NULL OR e.customerId = :customerId) AND " +
           "(:categoryId IS NULL OR e.categoryId = :categoryId) AND " +
           "(:status IS NULL OR :status = '' OR e.status = :status)")
    Page<Equipment> findByConditions(
            @Param("keyword") String keyword,
            @Param("customerId") Long customerId,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            Pageable pageable);
}
