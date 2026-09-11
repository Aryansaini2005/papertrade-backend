package com.aryan.tradewise_backend.portfolio.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PortfolioResponse {

    private String assetSymbol;

    private String assetName;

    private BigDecimal availableQuantity;

    private BigDecimal lockedQuantity;

    private BigDecimal currentPrice;

    private BigDecimal currentValue;
}