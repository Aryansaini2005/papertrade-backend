package com.aryan.tradewise_backend.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AIModelAnalysis {

    private String recommendation;

    private String reasoning;

    private double probability;

    private String riskLevel;

    private String analysis;

    private String holdingPeriod;

    private String invalidationCondition;
}
