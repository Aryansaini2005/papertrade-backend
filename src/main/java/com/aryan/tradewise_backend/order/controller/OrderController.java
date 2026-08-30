package com.aryan.tradewise_backend.order.controller;

import com.aryan.tradewise_backend.order.dto.CreateOrderRequest;
import com.aryan.tradewise_backend.order.dto.OrderResponse;
import com.aryan.tradewise_backend.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return orderService.createOrder(request);
    }

    @GetMapping
    public List<OrderResponse> getMyOrders() {

        return orderService.getMyOrders();
    }

    @DeleteMapping("/{orderId}")
    public OrderResponse cancelOrder(
            @PathVariable Long orderId) {

        return orderService.cancelOrder(orderId);
    }
}