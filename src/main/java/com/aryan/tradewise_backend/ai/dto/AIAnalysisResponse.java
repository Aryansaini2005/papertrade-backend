package com.aryan.tradewise_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AIAnalysisResponse {

    private String symbol;
    private BigDecimal currentPrice;
    private String recommendation;
    private String reasoning;
    private double probability;
    private String riskLevel;
    private String analysis;
    private String holdingPeriod;
    private String invalidationCondition;
    private String disclaimer;
    private LocalDateTime generatedAt;
}
