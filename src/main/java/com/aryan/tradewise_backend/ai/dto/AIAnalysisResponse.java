
package com.aryan.tradewise_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

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
}