package com.aryan.tradewise_backend.trade.dto;

import com.aryan.tradewise_backend.order.enums.OrderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TradeResponse {

    private Long id;

    private String assetSymbol;

    private String assetName;

    private OrderType orderType;

    private BigDecimal quantity;

    private BigDecimal executionPrice;

    private BigDecimal totalAmount;

    private LocalDateTime executedAt;
}