package com.maintenance.service;

import com.maintenance.common.BusinessException;
import com.maintenance.common.PageResult;
import com.maintenance.entity.Customer;
import com.maintenance.repository.CustomerRepository;
import com.maintenance.repository.EquipmentRepository;
import com.maintenance.repository.MaintenanceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EquipmentRepository equipmentRepository;
    private final MaintenanceOrderRepository maintenanceOrderRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public PageResult<Customer> findPage(Integer pageNum, Integer pageSize) {
        Page<Customer> page = customerRepository.findAll(
                PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("客户不存在，ID: " + id));
    }

    @Transactional
    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, Customer customer) {
        Customer existing = findById(id);
        existing.setName(customer.getName());
        existing.setContactPerson(customer.getContactPerson());
        existing.setContactPhone(customer.getContactPhone());
        existing.setAddress(customer.getAddress());
        existing.setRemark(customer.getRemark());
        return customerRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        long equipmentCount = equipmentRepository.countByCustomerId(id);
        if (equipmentCount > 0) {
            throw new BusinessException("该客户下存在 " + equipmentCount + " 台设备，无法删除。请先删除关联设备。");
        }
        long orderCount = maintenanceOrderRepository.countByCustomerId(id);
        if (orderCount > 0) {
            throw new BusinessException("该客户下存在 " + orderCount + " 条维保工单，无法删除。请先删除关联工单。");
        }
        customerRepository.deleteById(id);
    }
}
