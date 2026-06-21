package com.maintenance.repository;

import com.maintenance.entity.SparePart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    Page<SparePart> findByCategoryId(Long categoryId, Pageable pageable);

    List<SparePart> findByStockQuantityLessThanEqual(Integer safetyStock);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SparePart s WHERE s.id = :id")
    Optional<SparePart> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE SparePart s SET s.stockQuantity = s.stockQuantity + :delta WHERE s.id = :id AND s.stockQuantity + :delta >= 0")
    int adjustStockAtomically(@Param("id") Long id, @Param("delta") Integer delta);

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
