package com.maintenance.controller;

import com.maintenance.common.PageResult;
import com.maintenance.common.Result;
import com.maintenance.entity.SparePart;
import com.maintenance.entity.SparePartCategory;
import com.maintenance.service.SparePartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spare-parts")
@RequiredArgsConstructor
public class SparePartController {

    private final SparePartService sparePartService;

    @GetMapping("/categories")
    public Result<List<SparePartCategory>> findAllCategories() {
        return Result.success(sparePartService.findAllCategories());
    }

    @GetMapping
    public Result<PageResult<SparePart>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(sparePartService.findPage(pageNum, pageSize, keyword, categoryId));
    }

    @GetMapping("/low-stock")
    public Result<List<SparePart>> findLowStock() {
        return Result.success(sparePartService.findLowStock());
    }

    @GetMapping("/{id}")
    public Result<SparePart> findById(@PathVariable Long id) {
        return Result.success(sparePartService.findById(id));
    }

    @PostMapping
    public Result<SparePart> create(@RequestBody SparePart sparePart) {
        return Result.success(sparePartService.create(sparePart));
    }

    @PutMapping("/{id}")
    public Result<SparePart> update(@PathVariable Long id, @RequestBody SparePart sparePart) {
        return Result.success(sparePartService.update(id, sparePart));
    }

    @PutMapping("/{id}/stock")
    public Result<SparePart> updateStock(@PathVariable Long id,
                                         @RequestParam Integer quantity,
                                         @RequestParam(defaultValue = "true") Boolean isAdd) {
        return Result.success(sparePartService.updateStock(id, quantity, isAdd));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sparePartService.delete(id);
        return Result.success();
    }
}
