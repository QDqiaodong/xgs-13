package com.maintenance.repository;

import com.maintenance.entity.OrderPartConsumption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderPartConsumptionRepository extends JpaRepository<OrderPartConsumption, Long> {

    List<OrderPartConsumption> findByOrderId(Long orderId);

    Page<OrderPartConsumption> findByPartId(Long partId, Pageable pageable);
}
