package com.maintenance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "spare_part", indexes = {
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_part_model", columnList = "part_model"),
        @Index(name = "idx_stock_quantity", columnList = "stock_quantity")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_part_model", columnNames = "part_model")
})
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "part_model", nullable = false, length = 100)
    private String partModel;

    @Column(name = "part_name", nullable = false, length = 100)
    private String partName;

    @Column(length = 200)
    private String spec;

    @Column(length = 20)
    private String unit;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "safety_stock", nullable = false)
    private Integer safetyStock = 10;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(length = 200)
    private String supplier;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
