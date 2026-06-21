package com.maintenance.service;

import com.maintenance.common.PageResult;
import com.maintenance.entity.Equipment;
import com.maintenance.entity.EquipmentCategory;
import com.maintenance.repository.EquipmentCategoryRepository;
import com.maintenance.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository equipmentCategoryRepository;
    private final CacheService cacheService;

    public List<EquipmentCategory> findAllCategories() {
        return equipmentCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public EquipmentCategory findCategoryByCode(String code) {
        return equipmentCategoryRepository.findByCode(code)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("设备分类不存在，Code: " + code));
    }

    public PageResult<Equipment> findPage(Integer pageNum, Integer pageSize, String keyword,
                                          Long customerId, Long categoryId, String status) {
        Page<Equipment> page = equipmentRepository.findByConditions(
                keyword, customerId, categoryId, status,
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public Equipment findById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("设备不存在，ID: " + id));
    }

    public List<Equipment> findExpiringMaintenance() {
        LocalDate nextWeek = LocalDate.now().plusDays(30);
        return equipmentRepository.findByNextMaintenanceDateBefore(nextWeek);
    }

    @Transactional
    public Equipment create(Equipment equipment) {
        if (equipment.getNextMaintenanceDate() == null && equipment.getCategoryId() != null) {
            equipmentCategoryRepository.findById(equipment.getCategoryId()).ifPresent(category -> {
                Integer intervalDays = cacheService.getMaintenanceInterval(category.getCode());
                if (intervalDays == null) {
                    intervalDays = category.getMaintenanceIntervalDays();
                }
                LocalDate startDate = equipment.getLastMaintenanceDate() != null
                        ? equipment.getLastMaintenanceDate() : LocalDate.now();
                equipment.setNextMaintenanceDate(startDate.plusDays(intervalDays));
            });
        }
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment update(Long id, Equipment equipment) {
        Equipment existing = findById(id);
        existing.setCustomerId(equipment.getCustomerId());
        existing.setCategoryId(equipment.getCategoryId());
        existing.setEquipmentModel(equipment.getEquipmentModel());
        existing.setSerialNumber(equipment.getSerialNumber());
        existing.setInstallAddress(equipment.getInstallAddress());
        existing.setInstallDate(equipment.getInstallDate());
        existing.setStatus(equipment.getStatus());
        existing.setRemark(equipment.getRemark());

        if (equipment.getLastMaintenanceDate() != null
                && !equipment.getLastMaintenanceDate().equals(existing.getLastMaintenanceDate())) {
            existing.setLastMaintenanceDate(equipment.getLastMaintenanceDate());
            equipmentCategoryRepository.findById(existing.getCategoryId()).ifPresent(category -> {
                Integer intervalDays = cacheService.getMaintenanceInterval(category.getCode());
                if (intervalDays == null) {
                    intervalDays = category.getMaintenanceIntervalDays();
                }
                existing.setNextMaintenanceDate(equipment.getLastMaintenanceDate().plusDays(intervalDays));
            });
        }

        if (equipment.getNextMaintenanceDate() != null) {
            existing.setNextMaintenanceDate(equipment.getNextMaintenanceDate());
        }

        return equipmentRepository.save(existing);
    }

    @Transactional
    public Equipment updateLastMaintenanceDate(Long id, LocalDate lastMaintenanceDate) {
        Equipment equipment = findById(id);
        equipment.setLastMaintenanceDate(lastMaintenanceDate);

        equipmentCategoryRepository.findById(equipment.getCategoryId()).ifPresent(category -> {
            Integer intervalDays = cacheService.getMaintenanceInterval(category.getCode());
            if (intervalDays == null) {
                intervalDays = category.getMaintenanceIntervalDays();
            }
            equipment.setNextMaintenanceDate(lastMaintenanceDate.plusDays(intervalDays));
        });

        return equipmentRepository.save(equipment);
    }

    @Transactional
    public void delete(Long id) {
        equipmentRepository.deleteById(id);
    }
}
