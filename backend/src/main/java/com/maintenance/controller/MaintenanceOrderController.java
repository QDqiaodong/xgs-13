package com.maintenance.controller;

import com.maintenance.common.PageResult;
import com.maintenance.common.Result;
import com.maintenance.entity.MaintenanceOrder;
import com.maintenance.entity.OrderPartConsumption;
import com.maintenance.service.MaintenanceOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/maintenance-orders")
@RequiredArgsConstructor
public class MaintenanceOrderController {

    private final MaintenanceOrderService maintenanceOrderService;

    @GetMapping
    public Result<PageResult<MaintenanceOrder>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(maintenanceOrderService.findPage(
                pageNum, pageSize, keyword, customerId, equipmentId, orderStatus, orderType, startDate, endDate));
    }

    @GetMapping("/calendar")
    public Result<List<MaintenanceOrder>> findByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(maintenanceOrderService.findByDateRange(startDate, endDate));
    }

    @GetMapping("/{id}")
    public Result<MaintenanceOrder> findById(@PathVariable Long id) {
        return Result.success(maintenanceOrderService.findById(id));
    }

    @GetMapping("/{id}/parts")
    public Result<List<OrderPartConsumption>> findPartConsumptions(@PathVariable Long id) {
        return Result.success(maintenanceOrderService.findPartConsumptions(id));
    }

    @PostMapping
    public Result<MaintenanceOrder> create(@RequestBody MaintenanceOrder order) {
        return Result.success(maintenanceOrderService.create(order));
    }

    @PutMapping("/{id}")
    public Result<MaintenanceOrder> update(@PathVariable Long id, @RequestBody MaintenanceOrder order) {
        return Result.success(maintenanceOrderService.update(id, order));
    }

    @PutMapping("/{id}/status")
    public Result<MaintenanceOrder> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(maintenanceOrderService.updateStatus(id, status));
    }

    @PutMapping("/{id}/complete")
    public Result<MaintenanceOrder> completeOrder(@PathVariable Long id, @RequestBody MaintenanceOrder completionData) {
        return Result.success(maintenanceOrderService.completeOrder(id, completionData));
    }

    @PutMapping("/batch-status")
    public Result<Void> batchUpdateStatus(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        String status = (String) params.get("status");
        maintenanceOrderService.batchUpdateStatus(ids, status);
        return Result.success();
    }

    @PostMapping("/{id}/parts")
    public Result<OrderPartConsumption> addPartConsumption(
            @PathVariable Long id,
            @RequestBody OrderPartConsumption consumption) {
        return Result.success(maintenanceOrderService.addPartConsumption(id, consumption));
    }

    @DeleteMapping("/{orderId}/parts/{consumptionId}")
    public Result<Void> removePartConsumption(
            @PathVariable Long orderId,
            @PathVariable Long consumptionId) {
        maintenanceOrderService.removePartConsumption(consumptionId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        maintenanceOrderService.delete(id);
        return Result.success();
    }
}
