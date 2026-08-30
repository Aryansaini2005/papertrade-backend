package com.aryan.tradewise_backend.order.dto;

import com.aryan.tradewise_backend.order.enums.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Asset symbol is required")
    private String assetSymbol;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private BigDecimal quantity;
}