package com.maintenance.repository;

import com.maintenance.entity.SparePart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    Page<SparePart> findByCategoryId(Long categoryId, Pageable pageable);

    List<SparePart> findByStockQuantityLessThanEqual(Integer safetyStock);

    @Query("SELECT s FROM SparePart s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "s.partModel LIKE %:keyword% OR " +
           "s.partName LIKE %:keyword% OR " +
           "s.spec LIKE %:keyword%) AND " +
           "(:categoryId IS NULL OR s.categoryId = :categoryId)")
    Page<SparePart> findByConditions(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable);
}
