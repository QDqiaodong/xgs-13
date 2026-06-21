package com.maintenance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "maintenance_plan", indexes = {
        @Index(name = "idx_equipment_id", columnList = "equipment_id"),
        @Index(name = "idx_plan_date", columnList = "plan_date"),
        @Index(name = "idx_plan_status", columnList = "plan_status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_equipment_date_type", columnNames = {"equipment_id", "plan_date", "plan_type"})
})
public class MaintenancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "plan_type", nullable = false, length = 30)
    private String planType = "PERIODIC";

    @Column(name = "cycle_months", nullable = false)
    private Integer cycleMonths = 3;

    @Column(name = "plan_status", nullable = false, length = 30)
    private String planStatus = "PENDING";

    @Column(name = "order_id")
    private Long orderId;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
