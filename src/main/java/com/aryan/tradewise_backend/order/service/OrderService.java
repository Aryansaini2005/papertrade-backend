package com.aryan.tradewise_backend.order.service;

import com.aryan.tradewise_backend.order.dto.CreateOrderRequest;
import com.aryan.tradewise_backend.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    List<OrderResponse> getMyOrders();

    OrderResponse cancelOrder(Long orderId);

    OrderResponse executeOrder(Long orderId);
}