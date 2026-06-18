package com.maintenance.controller;

import com.maintenance.common.PageResult;
import com.maintenance.common.Result;
import com.maintenance.entity.Equipment;
import com.maintenance.entity.EquipmentCategory;
import com.maintenance.service.CacheService;
import com.maintenance.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final CacheService cacheService;

    @GetMapping("/categories")
    public Result<List<EquipmentCategory>> findAllCategories() {
        return Result.success(equipmentService.findAllCategories());
    }

    @GetMapping("/categories/{code}/interval")
    public Result<Integer> getMaintenanceInterval(@PathVariable String code) {
        Integer interval = cacheService.getMaintenanceInterval(code);
        if (interval == null) {
            EquipmentCategory category = equipmentService.findCategoryByCode(code);
            interval = category.getMaintenanceIntervalDays();
        }
        return Result.success(interval);
    }

    @GetMapping("/sparepart-template/{code}")
    public Result<Map<String, Object>> getSparePartTemplate(@PathVariable String code) {
        return Result.success(cacheService.getSparePartTemplate(code));
    }

    @GetMapping
    public Result<PageResult<Equipment>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        return Result.success(equipmentService.findPage(pageNum, pageSize, keyword, customerId, categoryId, status));
    }

    @GetMapping("/expiring")
    public Result<List<Equipment>> findExpiringMaintenance() {
        return Result.success(equipmentService.findExpiringMaintenance());
    }

    @GetMapping("/{id}")
    public Result<Equipment> findById(@PathVariable Long id) {
        return Result.success(equipmentService.findById(id));
    }

    @PostMapping
    public Result<Equipment> create(@RequestBody Equipment equipment) {
        return Result.success(equipmentService.create(equipment));
    }

    @PutMapping("/{id}")
    public Result<Equipment> update(@PathVariable Long id, @RequestBody Equipment equipment) {
        return Result.success(equipmentService.update(id, equipment));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return Result.success();
    }
}
