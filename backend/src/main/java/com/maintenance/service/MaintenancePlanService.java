package com.maintenance.service;

import com.maintenance.common.PageResult;
import com.maintenance.entity.MaintenancePlan;
import com.maintenance.repository.MaintenancePlanRepository;
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
public class MaintenancePlanService {

    private final MaintenancePlanRepository maintenancePlanRepository;

    public PageResult<MaintenancePlan> findPage(Integer pageNum, Integer pageSize, Long equipmentId,
                                                 String planStatus, LocalDate startDate, LocalDate endDate) {
        Page<MaintenancePlan> page = maintenancePlanRepository.findByConditions(
                equipmentId, planStatus, startDate, endDate,
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "planDate")));
        return PageResult.of(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public List<MaintenancePlan> findPlansByDateRange(LocalDate startDate, LocalDate endDate) {
        return maintenancePlanRepository.findByPlanDateBetweenAndPlanStatus(startDate, endDate, "PENDING");
    }

    public MaintenancePlan findById(Long id) {
        return maintenancePlanRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("维保计划不存在，ID: " + id));
    }

    @Transactional
    public MaintenancePlan create(MaintenancePlan plan) {
        plan.setPlanStatus("PENDING");
        return maintenancePlanRepository.save(plan);
    }

    @Transactional
    public MaintenancePlan update(Long id, MaintenancePlan plan) {
        MaintenancePlan existing = findById(id);
        existing.setEquipmentId(plan.getEquipmentId());
        existing.setPlanDate(plan.getPlanDate());
        existing.setPlanType(plan.getPlanType());
        existing.setCycleMonths(plan.getCycleMonths());
        existing.setRemark(plan.getRemark());
        return maintenancePlanRepository.save(existing);
    }

    @Transactional
    public MaintenancePlan completePlan(Long id, Long orderId) {
        MaintenancePlan plan = findById(id);
        plan.setPlanStatus("COMPLETED");
        plan.setOrderId(orderId);

        MaintenancePlan nextPlan = new MaintenancePlan();
        nextPlan.setEquipmentId(plan.getEquipmentId());
        nextPlan.setPlanDate(plan.getPlanDate().plusMonths(plan.getCycleMonths()));
        nextPlan.setPlanType(plan.getPlanType());
        nextPlan.setCycleMonths(plan.getCycleMonths());
        nextPlan.setPlanStatus("PENDING");
        maintenancePlanRepository.save(nextPlan);

        return maintenancePlanRepository.save(plan);
    }

    @Transactional
    public void delete(Long id) {
        maintenancePlanRepository.deleteById(id);
    }
}
