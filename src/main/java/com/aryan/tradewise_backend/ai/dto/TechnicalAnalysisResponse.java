package com.aryan.tradewise_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TechnicalAnalysisResponse {

    private String symbol;

    private BigDecimal currentPrice;

    private BigDecimal sma20;

    private BigDecimal sma50;

    private BigDecimal priceChangePercent;

    private String trend;
}
