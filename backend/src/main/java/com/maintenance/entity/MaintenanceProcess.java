package com.maintenance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "maintenance_process", indexes = {
        @Index(name = "idx_category_id", columnList = "category_id")
})
public class MaintenanceProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "process_code", nullable = false, unique = true, length = 50)
    private String processCode;

    @Column(name = "process_name", nullable = false, length = 100)
    private String processName;

    @Column(name = "process_content", columnDefinition = "TEXT")
    private String processContent;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
