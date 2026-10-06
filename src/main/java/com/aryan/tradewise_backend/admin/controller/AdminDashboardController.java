package com.aryan.tradewise_backend.admin.controller;

import com.aryan.tradewise_backend.admin.dto.AdminDashboardResponse;
import com.aryan.tradewise_backend.order.enums.OrderStatus;
import com.aryan.tradewise_backend.order.repository.OrderRepository;
import com.aryan.tradewise_backend.trade.repository.TradeRepository;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    public AdminDashboardController(
            UserRepository userRepository,
            OrderRepository orderRepository,
            TradeRepository tradeRepository) {

        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
    }

    @GetMapping
    public AdminDashboardResponse getDashboard() {

        return new AdminDashboardResponse(
                userRepository.count(),
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                tradeRepository.count()
        );
    }
}