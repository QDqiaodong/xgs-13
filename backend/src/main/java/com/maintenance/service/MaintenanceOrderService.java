package com.maintenance.service;

import com.maintenance.common.PageResult;
import com.maintenance.dto.BatchStatusCheckResult;
import com.maintenance.dto.OrderStatusCheckItem;
import com.maintenance.entity.Customer;
import com.maintenance.entity.Equipment;
import com.maintenance.entity.MaintenanceOrder;
import com.maintenance.entity.OrderPartConsumption;
import com.maintenance.enums.OrderStatusEnum;
import com.maintenance.repository.MaintenanceOrderRepository;
import com.maintenance.repository.OrderPartConsumptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceOrderService {

    private final MaintenanceOrderRepository maintenanceOrderRepository;
    private final OrderPartConsumptionRepository orderPartConsumptionRepository;
    private final SparePartService sparePartService;
    private final EquipmentService equipmentService;
    private final CustomerService customerService;

    private String generateOrderNo() {
        return "WO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public PageResult<MaintenanceOrder> findPage(Integer pageNum, Integer pageSize, String keyword,
                                                  Long customerId, Long equipmentId, String orderStatus,
                                                  String orderType, LocalDate startDate, LocalDate endDate) {
        Page<MaintenanceOrder> page = maintenanceOrderRepository.findByConditions(
                keyword, customerId, equipmentId, orderStatus, orderType, startDate, endDate,
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public List<MaintenanceOrder> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return maintenanceOrderRepository.findByPlanDateBetween(startDate, endDate);
    }

    public MaintenanceOrder findById(Long id) {
        return maintenanceOrderRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("维保工单不存在，ID: " + id));
    }

    public List<OrderPartConsumption> findPartConsumptions(Long orderId) {
        return orderPartConsumptionRepository.findByOrderId(orderId);
    }

    @Transactional
    public MaintenanceOrder create(MaintenanceOrder order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(generateOrderNo());
        }
        order.setOrderStatus("PENDING");
        return maintenanceOrderRepository.save(order);
    }

    @Transactional
    public MaintenanceOrder update(Long id, MaintenanceOrder order) {
        MaintenanceOrder existing = findById(id);
        if ("COMPLETED".equals(existing.getOrderStatus())) {
            existing.setRemark(order.getRemark());
        } else {
            existing.setEquipmentId(order.getEquipmentId());
            existing.setCustomerId(order.getCustomerId());
            existing.setOrderType(order.getOrderType());
            existing.setPlanDate(order.getPlanDate());
            existing.setActualDate(order.getActualDate());
            existing.setEngineer(order.getEngineer());
            existing.setWorkHours(order.getWorkHours());
            existing.setWorkContent(order.getWorkContent());
            existing.setFaultDescription(order.getFaultDescription());
            existing.setSolution(order.getSolution());
            existing.setRemark(order.getRemark());
        }
        return maintenanceOrderRepository.save(existing);
    }

    @Transactional
    public MaintenanceOrder updateStatus(Long id, String status) {
        MaintenanceOrder order = findById(id);
        if (!OrderStatusEnum.isValidTransition(order.getOrderStatus(), status)) {
            throw new IllegalArgumentException("不允许从 " + OrderStatusEnum.getLabel(order.getOrderStatus()) +
                    " 流转到 " + OrderStatusEnum.getLabel(status));
        }
        order.setOrderStatus(status);
        if ("COMPLETED".equals(status) && order.getActualDate() == null) {
            order.setActualDate(LocalDate.now());
            equipmentService.updateLastMaintenanceDate(order.getEquipmentId(), LocalDate.now());
        }
        return maintenanceOrderRepository.save(order);
    }

    @Transactional
    public MaintenanceOrder completeOrder(Long id, MaintenanceOrder completionData) {
        MaintenanceOrder order = findById(id);
        if (!OrderStatusEnum.isValidTransition(order.getOrderStatus(), "COMPLETED")) {
            throw new IllegalArgumentException("不允许从 " + OrderStatusEnum.getLabel(order.getOrderStatus()) +
                    " 流转到 已完成");
        }
        order.setOrderStatus("COMPLETED");
        order.setActualDate(completionData.getActualDate() != null ? completionData.getActualDate() : LocalDate.now());
        order.setEngineer(completionData.getEngineer());
        order.setWorkHours(completionData.getWorkHours());
        order.setWorkContent(completionData.getWorkContent());
        order.setFaultDescription(completionData.getFaultDescription());
        order.setSolution(completionData.getSolution());
        equipmentService.updateLastMaintenanceDate(order.getEquipmentId(), order.getActualDate());
        return maintenanceOrderRepository.save(order);
    }

    @Transactional
    public OrderPartConsumption addPartConsumption(Long orderId, OrderPartConsumption consumption) {
        consumption.setOrderId(orderId);
        sparePartService.updateStock(consumption.getPartId(), consumption.getQuantity(), false);
        return orderPartConsumptionRepository.save(consumption);
    }

    @Transactional
    public void removePartConsumption(Long consumptionId) {
        OrderPartConsumption consumption = orderPartConsumptionRepository.findById(consumptionId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("备件消耗记录不存在"));
        sparePartService.updateStock(consumption.getPartId(), consumption.getQuantity(), true);
        orderPartConsumptionRepository.deleteById(consumptionId);
    }

    @Transactional
    public void batchUpdateStatus(List<Long> ids, String status) {
        for (Long id : ids) {
            updateStatus(id, status);
        }
    }

    public BatchStatusCheckResult checkBatchStatusTransition(List<Long> ids, String targetStatus) {
        List<MaintenanceOrder> orders = maintenanceOrderRepository.findAllById(ids);

        List<Customer> customers = customerService.findAll();
        List<Equipment> equipments = equipmentService.findAll();

        Map<Long, String> customerNameMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));
        Map<Long, String> equipmentModelMap = equipments.stream()
                .collect(Collectors.toMap(Equipment::getId, Equipment::getEquipmentModel));

        List<OrderStatusCheckItem> validItems = new ArrayList<>();
        List<OrderStatusCheckItem> invalidItems = new ArrayList<>();

        for (MaintenanceOrder order : orders) {
            OrderStatusCheckItem item = new OrderStatusCheckItem();
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setCustomerId(order.getCustomerId());
            item.setCustomerName(customerNameMap.getOrDefault(order.getCustomerId(), "-"));
            item.setEquipmentId(order.getEquipmentId());
            item.setEquipmentModel(equipmentModelMap.getOrDefault(order.getEquipmentId(), "-"));
            item.setCurrentStatus(order.getOrderStatus());
            item.setCurrentStatusLabel(OrderStatusEnum.getLabel(order.getOrderStatus()));
            item.setTargetStatus(targetStatus);
            item.setTargetStatusLabel(OrderStatusEnum.getLabel(targetStatus));

            boolean canTransition = OrderStatusEnum.isValidTransition(order.getOrderStatus(), targetStatus);
            item.setCanTransition(canTransition);
            if (!canTransition) {
                item.setReason("不允许从 " + OrderStatusEnum.getLabel(order.getOrderStatus()) +
                        " 流转到 " + OrderStatusEnum.getLabel(targetStatus));
            }

            if (canTransition) {
                validItems.add(item);
            } else {
                invalidItems.add(item);
            }
        }

        BatchStatusCheckResult result = new BatchStatusCheckResult();
        result.setTargetStatus(targetStatus);
        result.setTargetStatusLabel(OrderStatusEnum.getLabel(targetStatus));
        result.setTotalCount(orders.size());
        result.setValidCount(validItems.size());
        result.setInvalidCount(invalidItems.size());
        result.setValidItems(validItems);
        result.setInvalidItems(invalidItems);

        return result;
    }

    @Transactional
    public void delete(Long id) {
        List<OrderPartConsumption> consumptions = orderPartConsumptionRepository.findByOrderId(id);
        for (OrderPartConsumption c : consumptions) {
            sparePartService.updateStock(c.getPartId(), c.getQuantity(), true);
        }
        orderPartConsumptionRepository.deleteAll(consumptions);
        maintenanceOrderRepository.deleteById(id);
    }
}
