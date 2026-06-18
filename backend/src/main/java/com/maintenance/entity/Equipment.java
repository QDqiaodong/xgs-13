package com.maintenance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "equipment", indexes = {
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_next_maintenance_date", columnList = "next_maintenance_date"),
        @Index(name = "idx_status", columnList = "status")
})
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "equipment_model", nullable = false, length = 100)
    private String equipmentModel;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "install_address", nullable = false, length = 500)
    private String installAddress;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Column(nullable = false, length = 20)
    private String status = "NORMAL";

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
