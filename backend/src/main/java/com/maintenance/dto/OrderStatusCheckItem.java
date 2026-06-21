package com.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusCheckItem {

    private Long orderId;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private Long equipmentId;
    private String equipmentModel;
    private String currentStatus;
    private String currentStatusLabel;
    private String targetStatus;
    private String targetStatusLabel;
    private Boolean canTransition;
    private String reason;
}
