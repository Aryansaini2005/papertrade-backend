package com.aryan.tradewise_backend.order.service;

import com.aryan.tradewise_backend.order.dto.CreateOrderRequest;
import com.aryan.tradewise_backend.order.dto.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);
}