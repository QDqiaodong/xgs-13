package com.maintenance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_part_consumption", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_part_id", columnList = "part_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_order_part", columnNames = {"order_id", "part_id"})
})
public class OrderPartConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "part_id", nullable = false)
    private Long partId;

    @Column(name = "part_name", length = 100)
    private String partName;

    @Column(name = "part_model", length = 100)
    private String partModel;

    @Column(length = 200)
    private String spec;

    @Column(length = 20)
    private String unit;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
