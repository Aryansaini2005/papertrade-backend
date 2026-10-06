package com.aryan.tradewise_backend.admin.controller;

import com.aryan.tradewise_backend.order.entity.Order;
import com.aryan.tradewise_backend.order.enums.OrderStatus;
import com.aryan.tradewise_backend.order.repository.OrderRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderRepository orderRepository;

    public AdminOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/pending")
    public List<Order> getPendingOrders() {

        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}