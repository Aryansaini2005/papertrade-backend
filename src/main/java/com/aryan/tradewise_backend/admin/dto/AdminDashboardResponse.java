package com.aryan.tradewise_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalOrders;
    private long pendingOrders;
    private long totalTrades;
}