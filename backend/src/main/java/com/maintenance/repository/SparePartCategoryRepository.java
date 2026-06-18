package com.maintenance.repository;

import com.maintenance.entity.SparePartCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SparePartCategoryRepository extends JpaRepository<SparePartCategory, Long> {

    Optional<SparePartCategory> findByCode(String code);
}
