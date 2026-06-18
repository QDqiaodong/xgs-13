package com.maintenance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "maintenance_order", indexes = {
        @Index(name = "idx_order_no", columnList = "order_no"),
        @Index(name = "idx_equipment_id", columnList = "equipment_id"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_order_status", columnList = "order_status"),
        @Index(name = "idx_plan_date", columnList = "plan_date"),
        @Index(name = "idx_actual_date", columnList = "actual_date"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class MaintenanceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_type", nullable = false, length = 30)
    private String orderType = "PERIODIC";

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus = "PENDING";

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Column(length = 50)
    private String engineer;

    @Column(name = "work_hours", precision = 5, scale = 1)
    private BigDecimal workHours;

    @Column(name = "work_content", columnDefinition = "TEXT")
    private String workContent;

    @Column(name = "fault_description", columnDefinition = "TEXT")
    private String faultDescription;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
