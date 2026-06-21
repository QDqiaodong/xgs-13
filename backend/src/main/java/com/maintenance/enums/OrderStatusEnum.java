package com.maintenance.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum OrderStatusEnum {

    PENDING("待处理"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    OrderStatusEnum(String label) {
        this.label = label;
    }

    public static final List<String> VALID_TRANSITIONS = Arrays.asList(
            "PENDING->IN_PROGRESS",
            "PENDING->CANCELLED",
            "IN_PROGRESS->COMPLETED",
            "IN_PROGRESS->CANCELLED",
            "IN_PROGRESS->PENDING",
            "COMPLETED->PENDING",
            "CANCELLED->PENDING"
    );

    public static boolean isValidTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }
        if (fromStatus.equals(toStatus)) {
            return false;
        }
        return VALID_TRANSITIONS.contains(fromStatus + "->" + toStatus);
    }

    public static String getLabel(String status) {
        if (status == null) {
            return "";
        }
        try {
            return OrderStatusEnum.valueOf(status).getLabel();
        } catch (IllegalArgumentException e) {
            return status;
        }
    }
}
