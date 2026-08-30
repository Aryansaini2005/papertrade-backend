package com.aryan.tradewise_backend.order.dto;

import com.aryan.tradewise_backend.order.enums.OrderStatus;
import com.aryan.tradewise_backend.order.enums.OrderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {

    private Long id;

    private String assetSymbol;

    private OrderType orderType;

    private BigDecimal quantity;

    private BigDecimal price;

    private OrderStatus status;

    private LocalDateTime createdAt;
}