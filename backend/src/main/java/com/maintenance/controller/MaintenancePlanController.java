package com.maintenance.controller;

import com.maintenance.common.PageResult;
import com.maintenance.common.Result;
import com.maintenance.entity.MaintenancePlan;
import com.maintenance.service.MaintenancePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/maintenance-plans")
@RequiredArgsConstructor
public class MaintenancePlanController {

    private final MaintenancePlanService maintenancePlanService;

    @GetMapping
    public Result<PageResult<MaintenancePlan>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String planStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(maintenancePlanService.findPage(pageNum, pageSize, equipmentId, planStatus, startDate, endDate));
    }

    @GetMapping("/calendar")
    public Result<List<MaintenancePlan>> findPlansByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(maintenancePlanService.findPlansByDateRange(startDate, endDate));
    }

    @GetMapping("/{id}")
    public Result<MaintenancePlan> findById(@PathVariable Long id) {
        return Result.success(maintenancePlanService.findById(id));
    }

    @PostMapping
    public Result<MaintenancePlan> create(@RequestBody MaintenancePlan plan) {
        return Result.success(maintenancePlanService.create(plan));
    }

    @PutMapping("/{id}")
    public Result<MaintenancePlan> update(@PathVariable Long id, @RequestBody MaintenancePlan plan) {
        return Result.success(maintenancePlanService.update(id, plan));
    }

    @PutMapping("/{id}/complete")
    public Result<MaintenancePlan> completePlan(@PathVariable Long id, @RequestParam(required = false) Long orderId) {
        return Result.success(maintenancePlanService.completePlan(id, orderId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        maintenancePlanService.delete(id);
        return Result.success();
    }
}
