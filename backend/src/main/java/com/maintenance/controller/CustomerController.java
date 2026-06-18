package com.maintenance.controller;

import com.maintenance.common.PageResult;
import com.maintenance.common.Result;
import com.maintenance.entity.Customer;
import com.maintenance.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public Result<PageResult<Customer>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(customerService.findPage(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Customer>> findAll() {
        return Result.success(customerService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Customer> findById(@PathVariable Long id) {
        return Result.success(customerService.findById(id));
    }

    @PostMapping
    public Result<Customer> create(@RequestBody Customer customer) {
        return Result.success(customerService.create(customer));
    }

    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
        return Result.success(customerService.update(id, customer));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.success();
    }
}
