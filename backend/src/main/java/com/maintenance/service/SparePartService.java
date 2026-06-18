package com.maintenance.service;

import com.maintenance.common.PageResult;
import com.maintenance.entity.SparePart;
import com.maintenance.entity.SparePartCategory;
import com.maintenance.repository.SparePartCategoryRepository;
import com.maintenance.repository.SparePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SparePartService {

    private final SparePartRepository sparePartRepository;
    private final SparePartCategoryRepository sparePartCategoryRepository;

    public List<SparePartCategory> findAllCategories() {
        return sparePartCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public PageResult<SparePart> findPage(Integer pageNum, Integer pageSize, String keyword, Long categoryId) {
        Page<SparePart> page = sparePartRepository.findByConditions(
                keyword, categoryId,
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public SparePart findById(Long id) {
        return sparePartRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("备件不存在，ID: " + id));
    }

    public List<SparePart> findLowStock() {
        return sparePartRepository.findAll().stream()
                .filter(p -> p.getStockQuantity() <= p.getSafetyStock())
                .toList();
    }

    @Transactional
    public SparePart create(SparePart sparePart) {
        return sparePartRepository.save(sparePart);
    }

    @Transactional
    public SparePart update(Long id, SparePart sparePart) {
        SparePart existing = findById(id);
        existing.setCategoryId(sparePart.getCategoryId());
        existing.setPartModel(sparePart.getPartModel());
        existing.setPartName(sparePart.getPartName());
        existing.setSpec(sparePart.getSpec());
        existing.setUnit(sparePart.getUnit());
        existing.setStockQuantity(sparePart.getStockQuantity());
        existing.setSafetyStock(sparePart.getSafetyStock());
        existing.setUnitPrice(sparePart.getUnitPrice());
        existing.setSupplier(sparePart.getSupplier());
        existing.setRemark(sparePart.getRemark());
        return sparePartRepository.save(existing);
    }

    @Transactional
    public SparePart updateStock(Long id, Integer quantity, boolean isAdd) {
        SparePart sparePart = findById(id);
        int newStock = isAdd
                ? sparePart.getStockQuantity() + quantity
                : sparePart.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("库存不足，当前库存: " + sparePart.getStockQuantity());
        }
        sparePart.setStockQuantity(newStock);
        return sparePartRepository.save(sparePart);
    }

    @Transactional
    public void delete(Long id) {
        sparePartRepository.deleteById(id);
    }
}
