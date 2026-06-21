package com.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchStatusCheckResult {

    private String targetStatus;
    private String targetStatusLabel;
    private Integer totalCount;
    private Integer validCount;
    private Integer invalidCount;
    private List<OrderStatusCheckItem> validItems;
    private List<OrderStatusCheckItem> invalidItems;
}
